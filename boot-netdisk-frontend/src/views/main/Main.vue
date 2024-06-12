<template>
  <div>
    <div class="top">
      <!-- Action Button -->
      <div class="top-op">
        <div class="btn">
          <el-upload
              :show-file-list="false"
              :with-credentials="true"
              :multiple="true"
              :http-request="uploadFile"
              :accept="fileAccept"
          >
            <el-button type="primary">
              <span class="iconfont icon-upload"></span>Upload
            </el-button>
          </el-upload>
        </div>
        <!-- You can create a new folder only when the category is All-->
        <el-button type="success" @click="newFloder">
          <span class="iconfont icon-folder-add"></span>Create a new folder
        </el-button>
        <el-button
            type="danger"
            :disabled="selectedIdList.length == 0"
            @click="delFileBatch"
        >
          <span class="iconfont icon-del"></span>Batch delete
        </el-button>
        <el-button
            type="warning"
            :disabled="selectedIdList.length == 0"
            @click="moveFileBatch"
        >
          <span class="iconfont icon-move"></span>Batch move
        </el-button>
        <div class="search-panel">
          <el-input
            clearable
            placeholder="search my file"
            v-model="fileNameFuzzy"
            @keyup.enter="search"
          >
            <template #suffix>
              <i class="iconfont icon-search" @click="search"></i>
            </template>
          </el-input>
        </div>
        <div class="iconfont icon-refresh" @click="loadDataList"></div>
      </div>

      <!-- navigation bar -->
      <Navigation ref="navigationRef" @navChange="navChange"></Navigation>
    </div>

    <!-- file list -->
    <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
      <Table
          ref="dataTableRef"
          :columns="columns"
          :dataSource="tableData"
          :fetch="loadDataList"
          :initFetch="false"
          :options="tableOptions"
          @rowSelected="rowSelected"
      >
        <template #fileName="{ index, row }">
          <div
              class="file-item"
              @mouseenter="showOp(row, 1)"
              @mouseleave="showOp(row, 0)"
          >
            <!-- The icon of the picture and video is the cover image, and the transcoding is successful -->
            <template
                v-if="(row.fileType == 1 || row.fileType == 3) && row.status == 2"
            >
              <Icon :cover="row.fileCover" :width="32"></Icon>
            </template>
            <template v-else>
              <!-- Other files display icons according to file type -->
              <Icon v-if="row.fileClass == 1" :fileType="row.fileType"></Icon>
              <!-- Folder icon fileType=0 -->
              <Icon v-if="row.fileClass == 2" :fileType="0"></Icon>
            </template>
            <!-- Do not display file name during row editing -->
            <span class="file-name" v-if="!row.showEdit" :title="row.fileName">
<span @click="clickFile(row)">{{ row.fileName }}</span>
              <!-- Transcoding status -->
<span v-if="row.status == 0" class="transfer-status">Transcoding in progress</span>
<span v-if="row.status == 1" class="transfer-fail">Transcoding failed</span>
</span>

            <!-- Row Editing - Create New Folder, Rename -->
            <div class="edit-panel" v-if="row.showEdit">
              <el-input
                  v-model.trim="row.fileNameReal"
                  ref="editNameRef"
                  :maxLength="180"
                  @keyup.enter="saveNameEdit(index)"
              >
                <template #suffix>{{ row.fileSuffix }}</template>
              </el-input>
              <span
                  :class="[
'iconfont icon-right1',
row.fileNameReal ? '' : 'not-allow',
]"
                  @click="saveNameEdit(index)"
              ></span>
              <span
                  class="iconfont icon-error"
                  @click="cancelNameEdit(index)"
              ></span>
            </div>

            <span class="op">
<template v-if="row.showOp && row.fileId && row.status == 2">
<span
    class="iconfont icon-share1"
    title="Share"
    @click="share(row)"
></span>
  <!-- Only files can be downloaded -->
<span
    class="iconfont icon-download"
    title="Download"
    @click="download(row)"
    v-if="row.fileClass == 1"
></span>
<span
    class="iconfont icon-del"
    title="Delete"
    @click="delFile(row)"
></span>
<span
    class="iconfont icon-edit"
    title="Rename"
    @click="rename(row)"
></span>
<span
    class="iconfont icon-move"
    title="Move"
    @click="moveFile(row)"
></span>
</template>
</span>
          </div>
        </template>
        <template #fileSize="{ index, row }">
<span v-if="row.fileSize">{{
    proxy.Utils.size2Str(row.fileSize)
  }}</span>
        </template>
      </Table>
    </div>
    <div class="no-data" v-else>
      <div class="no-data-inner">
        <Icon iconName="no_data" :width="120" fit="fill"></Icon>
        <div class="tips">The current directory is empty, upload your first file~</div>
        <div class="op-list">
          <el-upload
              :show-file-list="false"
              :with-credentials="true"
              :multiple="true"
              :http-request="uploadFile"
              :accept="fileAccept"
          >
            <div class="op-item">
              <Icon iconName="file" :width="60"></Icon>
              <div>Upload file</div>
            </div>
            <div class="op-item" v-if="category == 'all'" @click="newFloder">
              <Icon iconName="folder" :width="60"></Icon>
              <div>Create a new folder</div>
            </div>
          </el-upload>
        </div>
      </div>
    </div>

    <!-- 文件夹选择框组件 -->
    <FolderSelect
      ref="folderSelectRef"
      @folderSelect="moveFileDone"
    ></FolderSelect>

    <!-- 文件预览组件 -->
    <Preview ref="previewRef"></Preview>

    <!-- 文件分享弹窗 -->
    <ShareFile ref="shareRef"></ShareFile>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance, nextTick, computed } from "vue";
import categoryInfo from "@/assets/category.js";
import ShareFile from "./ShareFile.vue";

const { proxy } = getCurrentInstance();

const api = {
  loadDataList: "/file/loadDataList",
  newFolder: "/file/newFolder",
  rename: "/file/rename",
  deleteFile: "/file/deleteFile",
  moveFile: "/file/moveFile",
  getFolderList: "/file/getFolderList",
  createDownloadCode: "/file/createDownloadCode",
};

const columns = [
  {
    label: "file name",
    prop: "fileName",
    scopedSlots: "fileName",
    minWidth: 360,
  },
  {
    label: "modification time",
    prop: "updateTime",
    width: 200,
  },
  {
    label: "size",
    prop: "fileSize",
    width: 200,
    scopedSlots: "fileSize",
  },
];

const dataTableRef = ref({});
const tableData = ref({});
const tableOptions = ref({
  exHeight: 50,
  selectType: "checkbox",
});

// 文件过滤
const fileAccept = computed(() => {
  const categoryItem = categoryInfo[category.value];
  return categoryItem ? categoryItem.accept : "*";
});

const fileNameFuzzy = ref();
const category = ref();
const showLoading = ref(true);

// 当前所在目录
const currentFolder = ref({ fileId: "0" });

// 加载文件列表
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
    fileNameFuzzy: fileNameFuzzy.value,
    filePid: currentFolder.value.fileId,
    category: category.value,
  };

  // 查看分类文件不限制父类ID
  if (params.category !== "all") {
    delete params.filePid;
  }

  let result = await proxy.Request({
    url: api.loadDataList,
    showLoading: showLoading.value,
    params: params,
  });

  if (!result) {
    return;
  }

  tableData.value = result.data;
};

// 重新加载数据
const reload = () => {
  showLoading.value = false;
  loadDataList();
};
defineExpose({ reload });

// 搜索文件
const search = () => {
  showLoading.value = true;
  loadDataList();
};

const previewRef = ref();
// 点击文件
const clickFile = (data) => {
  // 文件夹点击进入
  if (data.fileClass == 2) {
    navigationRef.value.openFolder(data);
    return;
  }

  // 文件点击进行预览
  if (data.status != 2) {
    proxy.Message.warning("The file has not been converted and cannot be previewed. Please refresh");
    return;
  }

  previewRef.value.showPreview(data, 0);
};

const navigationRef = ref();
// 导航回调方法
const navChange = (data) => {
  const { categoryId, currFolder } = data;
  currentFolder.value = currFolder;
  category.value = categoryId;

  loadDataList();
};

const emit = defineEmits(["uploadFile"]);
// 上传文件
const uploadFile = (fileData) => {
  // 调用父组件方法
  emit("uploadFile", {
    file: fileData.file,
    filePid: currentFolder.value.fileId,
  });
};

// 已选中的文件列表
const selectedIdList = ref([]);

// 选中文件操作
const rowSelected = (rows) => {
  selectedIdList.value = [];
  rows.forEach((item) => {
    selectedIdList.value.push(item.fileId);
  });
};

// 展示操作按钮
const showOp = (row, type) => {
  // 遍历所有文件列表，隐藏全部文件的操作按钮
  tableData.value.list.forEach((item) => {
    item.showOp = false;
  });
  // 显示当前文件的操作按钮
  if (type == 1) {
    row.showOp = true;
  }
};

const shareRef = ref();
// 分享文件
const share = (row) => {
  shareRef.value.show(row);
};

// 下载文件
// createDownloadCode: "/file/createDownloadCode",

const download = async (row) => {
  let result = await proxy.Request({
    url: api.createDownloadCode + "/" + row.fileId,
  });

  if (!result) {
    return;
  }

  window.location.href = proxy.globalInfo.downloadUrl + result.data;
};

// 删除文件
const delFile = (row) => {
  proxy.Confirm(
      `Are you sure you want to delete [${row.fileName}]? Deleted files can be restored through the Recycle Bin within 10 days`,
    async () => {
      let result = await proxy.Request({
        url: api.deleteFile,
        params: {
          fileIds: row.fileId,
        },
      });

      if (!result) {
        return;
      }

      loadDataList();
    }
  );
};

// 批量删除文件
const delFileBatch = () => {
  if (selectedIdList.value.length == 0) {
    return;
  }

  proxy.Confirm(
    "Are you sure you want to delete these files? Deleted files can be restored through the Recycle Bin within 10 days.",
    async () => {
      let result = await proxy.Request({
        url: api.deleteFile,
        params: {
          fileIds: selectedIdList.value.join(","),
        },
      });

      if (!result) {
        return;
      }

      loadDataList();
    }
  );
};

// 行编辑状态
const editPanelFlag = ref(false);
const editNameRef = ref();
// 新建文件夹
const newFloder = () => {
  if (editPanelFlag.value) {
    return;
  }

  tableData.value.list.forEach((ele) => {
    ele.showEdit = false;
  });
  editPanelFlag.value = true;
  // 文件列表顶部插入一条记录
  tableData.value.list.unshift({
    showEdit: true,
    fileClass: 2,
    fileId: "",
    filePid: currentFolder.value.fileId,
  });

  nextTick(() => {
    // 获取光标焦点
    editNameRef.value.focus();
  });
};

// 重命名
const rename = (row) => {
  if (tableData.value.list[0].fileId == "") {
    tableData.value.list.splice(0, 1);
  }
  tableData.value.list.forEach((ele) => {
    ele.showEdit = false;
  });
  row.showEdit = true;

  // 截取文件名和后缀
  if (row.fileClass == 1) {
    let index = row.fileName.lastIndexOf(".");
    row.fileNameReal = row.fileName.substring(0, index);
    row.fileSuffix = row.fileName.substring(index);
  } else {
    row.fileNameReal = row.fileName;
    row.fileSuffix = "";
  }
  editPanelFlag.value = true;

  nextTick(() => {
    // 获取光标焦点
    editNameRef.value.focus();
  });
};

// 保存编辑
const saveNameEdit = async (index) => {
  const { fileId, filePid, fileNameReal } = tableData.value.list[index];
  if (fileNameReal == "" || fileNameReal.indexOf("/") != -1) {
    proxy.Message.warning("The file name cannot be empty and cannot contain/");
    return;
  }

  let url = "";
  // 新建文件夹
  if (fileId == "") {
    url = api.newFolder;
  } else {
    // 重命名
    url = api.rename;
  }

  let result = await proxy.Request({
    url: url,
    params: {
      fileId: fileId,
      filePid: filePid,
      fileName: fileNameReal,
    },
  });

  if (!result) {
    return;
  }

  tableData.value.list[index] = result.data;
  editPanelFlag.value = false;
};

// Cancel Edit
const cancelNameEdit = (index) => {
  const fileData = tableData.value.list[index];
  // 存在fileId则为重命名，关闭编辑
  if (fileData.fileId) {
    fileData.showEdit = false;
  } else {
    // 否则为新建文件夹，删除文件列表
    tableData.value.list.splice(index, 1);
  }
  editPanelFlag.value = false;
};

const folderSelectRef = ref();
const moveFileIdList = ref([]); // 要移动的文件

// 移动文件
const moveFile = (row) => {
  moveFileIdList.value = [row.fileId];
  folderSelectRef.value.show(row.fileId);
};

// 批量移动文件
const moveFileBatch = () => {
  if (selectedIdList.value.length == 0) {
    return;
  }
  moveFileIdList.value = selectedIdList.value;
  folderSelectRef.value.show(moveFileIdList.value.join(","));
};

// 移动文件操作
const moveFileDone = async (folderId) => {
  if (currentFolder.value.fileId == folderId) {
    proxy.Message.warning("The file is in the current directory and does not need to be moved");
    return;
  }

  let result = await proxy.Request({
    url: api.moveFile,
    params: {
      filePid: folderId,
      fileIds: moveFileIdList.value.join(","),
    },
  });

  if (!result) {
    return;
  }

  folderSelectRef.value.close();

  loadDataList();
};
</script>
<style lang="scss" scoped>
@import "@/assets/file_list.scss";
</style>
