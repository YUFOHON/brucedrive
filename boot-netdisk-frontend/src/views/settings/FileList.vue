<template>
  <div>
    <div class="top">
      <!-- Operation button -->
      <div class="top-op">
        <el-button
            type="danger"
            :disabled="selectedIdList.length == 0"
            @click="delFileBatch"
        >
          <span class="iconfont icon-del"></span>Batch delete
        </el-button>
        <div class="search-panel">
          <el-input
              clearable
              placeholder="Search my files"
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

      <!-- Navigation bar -->
      <Navigation ref="navigationRef" @navChange="navChange"></Navigation>
    </div>
    <!-- File list -->
    <div class="file-list">
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

            <span class="file-name" :title="row.fileName">
<span @click="clickFile(row)">{{ row.fileName }}</span>
              <!-- Transcoding status -->
<span v-if="row.status == 0" class="transfer-status">Transcoding</span>
<span v-if="row.status == 1" class="transfer-fail">Transcoding failed</span>
</span>

            <span class="op">
<!-- fileId exists (no fileId when creating a new folder), status is successful transcoding -->
<template v-if="row.showOp && row.fileId && row.status == 2">
<!-- Only files can be downloaded -->
<span
    class="iconfont icon-download"
    title="Download"
    @click="download(row)"
    v-if="row.fileClass == 1"
>Download</span
>
<span
    class="iconfont icon-del"
    title="Delete"
    @click="delFile(row)"
>Delete</span
>
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

    <!-- File preview component -->
    <Preview ref="previewRef"></Preview>
  </div>
</template>
<script setup>
import {ref, getCurrentInstance} from "vue";

const {proxy} = getCurrentInstance();

const api = {
  loadDataList: "/admin/loadFileList",
  deleteFile: "/admin/deleteFile",
  createDownloadCode: "/admin/createDownloadCode",
};

const columns = [
  {
    label: "file name",
    prop: "fileName",
    scopedSlots: "fileName",
    minWidth: 360,
  },
  {
    label: "publisher",
    prop: "nickName",
    width: 200,
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

const fileNameFuzzy = ref();
const showLoading = ref(true);

// 当前目录
const currentFolder = ref({fileId: "0"});

// 加载文件列表
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
    fileNameFuzzy: fileNameFuzzy.value,
    filePid: currentFolder.value.fileId,
  };

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
    proxy.Message.warning("The file has not been converted and cannot be previewed.");
    return;
  }

  previewRef.value.showPreview(data, 1);
};

const navigationRef = ref();
// 导航回调方法
const navChange = (data) => {
  const {currFolder} = data;
  currentFolder.value = currFolder;

  loadDataList();
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

// 已选中的文件列表
const selectedIdList = ref([]);

// 选中文件操作
const rowSelected = (rows) => {
  selectedIdList.value = [];
  rows.forEach((item) => {
    selectedIdList.value.push(item.fileId + "_" + item.userId);
  });
};

// 下载文件
const download = async (row) => {
  let result = await proxy.Request({
    url: api.createDownloadCode + "/" + row.userId + "/" + row.fileId,
  });

  if (!result) {
    return;
  }

  window.location.href = proxy.globalInfo.downloadUrl + result.data;
};

// Delete files
const delFile = (row) => {
  proxy.Confirm(
      `Are you sure you want to delete [${row.fileName}]? Deleted files cannot be recovered`,
      async () => {
        let result = await proxy.Request({
          url: api.deleteFile,
          params: {
            fileIdAndUserIds: row.fileId + "_" + row.userId,
          },
        });

        if (!result) {
          return;
        }

        loadDataList();
      }
  );
};

// Batch delete files
const delFileBatch = () => {
  if (selectedIdList.value.length == 0) {
    return;
  }

  proxy.Confirm("Are you sure you want to delete these files? Deleted files cannot be recovered", async () => {
    let result = await proxy.Request({
      url: api.deleteFile,
      params: {
        fileIdAndUserIds: selectedIdList.value.join(","),
      },
    });

    if (!result) {
      return;
    }

    loadDataList();
  });
};
</script>
<style lang="scss" scoped>
@import "@/assets/file_list.scss";

.file-list {
  margin-top: 10px;

  .file-item {
    .op {
      width: 160px;
    }
  }
}
</style>
