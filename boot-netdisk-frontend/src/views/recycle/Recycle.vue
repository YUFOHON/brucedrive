<template>
  <div>
    <div class="top">
      <el-button
          type="success"
          :disabled="selectedIdList.length == 0"
          @click="revertFileBatch"
      >
        <span class="iconfont icon-revert">Restore</span>
      </el-button>
      <el-button
          type="danger"
          :disabled="selectedIdList.length == 0"
          @click="delFileBatch"
      >
        <span class="iconfont icon-del">Completely delete</span>
      </el-button>
    </div>
    <div class="file-list">
      <Table
          ref="dataTableRef"
          :columns="columns"
          :dataSource="tableData"
          :fetch="loadDataList"
          :initFetch="true"
          :options="tableOptions"
          @rowSelected="rowSelected"
      >
        <template #fileName="{ index, row }">
          <div
              class="file-item"
              @mouseenter="showOp(row, 1)"
              @mouseleave="showOp(row, 0)"
          >
            <!-- The icon of the picture or video is the cover image. -->
            <template
                v-if="(row.fileType == 1 || row.fileType == 3) && row.status != 0"
            >
              <Icon :cover="row.fileCover" :width="32"></Icon>
            </template>
            <template v-else>
              <!-- Other files display icons according to file type -->
              <Icon v-if="row.fileClass == 1" :fileType="row.fileType"></Icon>
              <!-- Folder icon fileType=0 -->
              <Icon v-if="row.fileClass == 2" :fileType="0"></Icon>
            </template>
            <span class="file-name" :title="row.fileName">{{
                row.fileName
              }}</span>
            <span class="op">
<template v-if="row.showOp">
<span
    class="iconfont icon-revert"
    title="Restore"
    @click="revertFile(row)"
>Restore</span
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
  </div>
</template>
<script setup>
import { ref, getCurrentInstance } from "vue";

const { proxy } = getCurrentInstance();

const api = {
  loadDataList: "/recycle/loadDataList",
  revertFile: "/recycle/revertFile",
  deleteFile: "/recycle/deleteFile",
};

const columns = [
  {
    label: "file name",
    prop: "fileName",
    scopedSlots: "fileName",
    minWidth: 360,
  },
  {
    label: "deletion time",
    prop: "recycleTime",
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
  exHeight: 20,
  selectType: "checkbox",
});

// Load file list
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
  };

// View category files without limiting parent category ID
  if (params.category !== "all") {
    delete params.filePid;
  }

  let result = await proxy.Request({
    url: api.loadDataList,
    params: params,
  });

  if (!result) {
    return;
  }

  console.log(result.data);
  tableData.value = result.data;
};

// Selected file list
const selectedIdList = ref([]);

// Select file operation
const rowSelected = (rows) => {
  selectedIdList.value = [];
  rows.forEach((item) => {
    selectedIdList.value.push(item.fileId);
  });
};

// Display operation button
const showOp = (row, type) => {
// Traverse all file lists and hide the operation buttons of all files
  tableData.value.list.forEach((item) => {
    item.showOp = false;
  });
// Display the operation button of the current file
  if (type == 1) {
    row.showOp = true;
  }
};

// Restore file
const revertFile = (row) => {
  proxy.Confirm(`Are you sure you want to restore [${row.fileName}]? `, async () => {
    let result = await proxy.Request({
      url: api.revertFile,
      params: {
        fileIds: row.fileId,
      },
    });

    if (!result) {
      return;
    }

    loadDataList();
  });
};

// Batch restore files
const revertFileBatch = () => {
  proxy.Confirm(`Are you sure you want to restore these files?`, async () => {
    let result = await proxy.Request({
      url: api.revertFile,
      params: {
        fileIds: selectedIdList.value.join(","),
      },
    });

    if (!result) {
      return;
    }

    loadDataList();
  });
};

const emit = defineEmits(["reloadUserSpace"])
// Delete files
const delFile = (row) => {
  proxy.Confirm(
      `Are you sure you want to delete [${row.fileName}]? It cannot be restored after deletion`,
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

// Call the parent component method to reload the user space size
        emit("reloadUserSpace")
      }
  );
};

// Batch delete files
const delFileBatch = () => {
  proxy.Confirm(`Are you sure you want to delete these files? It cannot be restored after deletion`, async () => {
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

    emit("reloadUserSpace")
  });
};
</script>
<style lang="scss" scoped>
@import "@/assets/file_list.scss";

.file-list {
  margin-top: 10px;

  .file-item {
    .op {
      width: 150px;
    }
  }
}
</style>
