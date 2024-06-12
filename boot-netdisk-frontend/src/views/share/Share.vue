<template>
  <div>
    <div class="top">
      <el-button
          type="primary"
          :disabled="selectedIdList.length == 0"
          @click="cancelShareBatch"
      >
        <span class="iconfont icon-cancel"></span>Cancel sharing
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
            <!-- The icon of the picture and video is the cover picture, which is successfully transcoded -->
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
    class="iconfont icon-link"
    title="Copy link"
    @click="copy(row)"
>Copy link</span
>
<span
    class="iconfont icon-cancel"
    title="Cancel share"
    @click="cancelShare(row)"
>Cancel share</span
>
</template>
</span>
          </div>
        </template>
        <template #expireTime="{ index, row }">
          {{ row.validType == 3 ? "Permanent" : row.expireTime }}
        </template>
      </Table>
    </div>
  </div></template>
<script setup>
import { ref, getCurrentInstance } from "vue";
import useClipboard from "vue-clipboard3";

const { proxy } = getCurrentInstance();
const { toClipboard } = useClipboard();

const api = {
  loadDataList: "/share/loadDataList",
  cancelShare: "/share/cancelShare",
};

const columns = [
  {
    label: "file name",
    prop: "fileName",
    scopedSlots: "fileName",
    minWidth: 360,
  },
  {
    label: "sharing time",
    prop: "createTime",
    width: 200,
  },
  {
    label: "expiration time",
    prop: "expireTime",
    width: 200,
    scopedSlots: "expireTime",
  },
  {
    label: "view count",
    prop: "showCount",
    width: 200,
  },
];
const dataTableRef = ref({});
const tableData = ref({});
const tableOptions = ref({
  exHeight: 20,
  selectType: "checkbox",
});

// 加载文件列表
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
  };

  // 查看分类文件不限制父类ID
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

// 已选中的文件列表
const selectedIdList = ref([]);

// 选中文件操作
const rowSelected = (rows) => {
  selectedIdList.value = [];
  rows.forEach((item) => {
    selectedIdList.value.push(item.shareId);
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
const baseUrl = import.meta.env.VITE_BASE_URL || 'brucedrive';

const shareUrl = ref( document.location.origin +"/"+ baseUrl+ "/share/");

// 复制链接和提取码
const copy = async (data) => {
  console.log(baseUrl);
  const fullShareUrl = `${shareUrl.value}${data.shareId}`;
  await toClipboard(
      `Link:${fullShareUrl} Extraction code:${data.code}`  );
  proxy.Message.success("Copy Success");
};

const cancelShareIdList = ref([]); // 要取消的分享

// 取消分享
const cancelShare = (row) => {
  cancelShareIdList.value = [row.shareId];
  cancelShareDone();
};

// 批量取消分享
const cancelShareBatch = () => {
  if (selectedIdList.value.length == 0) {
    return;
  }
  cancelShareIdList.value = selectedIdList.value;
  cancelShareDone();
};

// 取消分享操作
const cancelShareDone = () => {
  proxy.Confirm("Are you sure you want to cancel sharing?？", async () => {
    let result = await proxy.Request({
      url: api.cancelShare,
      params: {
        shareIds: cancelShareIdList.value.join(","),
      },
    });

    if (!result) {
      return;
    }
    proxy.Message.success("Cancel sharing successfully");
    loadDataList();
  });
};
</script>
<style lang="scss" scoped>
@import "@/assets/file_list.scss";

.file-list {
  margin-top: 10px;

  .file-item {
    .file-name {
      span {
        &:hover {
          color: #494944;
        }
      }
    }

    .op {
      width: 190px;
    }
  }
}
</style>
