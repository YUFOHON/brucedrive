<template>
  <div>
    <div class="header">
      <div class="header-content">
        <div class="logo" @click="jump">
          <span class="iconfont"></span>
          <span class="name">Bruce drive</span>
        </div>
      </div>
    </div>
    <div class="share-body">
      <template v-if="Object.keys(shareInfo).length == 0">
        <div class="no-data">
          <div class="no-data-inner">
            <Icon iconName="no_data" :width="150" fit="fill"></Icon>
            <div class="tips">{{ errorMsg }}</div>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="share-panel">
          <div class="share-user-info">
            <div class="avatar">
              <Avatar
                  :userId="shareInfo.userId"
                  :avatar="shareInfo.avatar"
                  :width="50"
              ></Avatar>
            </div>
            <div class="share-info">
              <div class="user-info">
                <span class="nick-name">{{ shareInfo.nickName }}</span>
                <span class="share-time"
                >Shared at: {{ shareInfo.createTime }}</span
                >
              </div>
              <div class="file-name">Shared file: {{ shareInfo.fileName }}</div>
            </div>
          </div>
          <div class="share-op-btn">
            <el-button
                type="primary"
                @click="cancelShare"
                v-if="shareInfo.currentUser"
            ><span class="iconfont icon-cancel"></span>Cancel sharing</el-button
            >
            <el-button
                type="primary"
                @click="save2MyPanBatch"
                :disabled="selectedIdList.length == 0"
                v-else
            >
              <span class="iconfont icon-import"></span>Save to my network disk
            </el-button>
          </div>
        </div>
        <Navigation
            ref="navigationRef"
            @navChange="navChange"
            :shareId="shareId"
        ></Navigation>
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
                    v-if="
(row.fileType == 1 || row.fileType == 3) && row.status == 2
"
                >
                  <Icon :cover="row.fileCover" :width="32"></Icon>
                </template>
                <template v-else>
                  <!-- Other files display icons according to file types -->
                  <Icon
                      v-if="row.fileClass == 1"
                      :fileType="row.fileType"
                  ></Icon>
                  <!-- Folder icon fileType=0 -->
                  <Icon v-if="row.fileClass == 2" :fileType="0"></Icon>
                </template>

                <span class="file-name" :title="row.fileName">
<span @click="clickFile(row)">{{ row.fileName }}</span>
</span>

                <span class="op">
<template v-if="row.showOp">
<!-- Only files can be downloaded -->
<span
    class="iconfont icon-download"
    title="Download"
    @click="download(row)"
    v-if="row.fileClass == 1"
>Download</span
>
<span
    class="iconfont icon-import"
    title="Delete"
    @click="save2MyPan(row)"
    v-if="!shareInfo.currentUser"
>Save to my network disk</span
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
      </template>

      <!-- Folder selection box component -->
      <FolderSelect
          ref="folderSelectRef"
          @folderSelect="save2MyPanDone"
      ></FolderSelect>

      <!-- File preview component -->
      <Preview ref="previewRef"></Preview>
    </div>
  </div>
</template>


<script setup>
import { ref, getCurrentInstance } from "vue";
import { useRouter, useRoute } from "vue-router";

const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();

const api = {
  getShareInfo: "/webShare/getShareInfo",
  loadFileList: "/webShare/loadFileList",
  createDownloadCode: "/webShare/createDownloadCode",
  saveShare: "/webShare/saveShare",
  cancelShare: "/share/cancelShare",
};

const shareInfo = ref({});
const shareId = route.params.shareId;
const errorMsg = ref(null);

// 获取分享登录信息
const getShareInfo = async () => {
  let result = await proxy.Request({
    url: api.getShareInfo,
    showLoading: false,
    params: {
      shareId: shareId,
    },
    showError: false,
    errorCallback: (error) => {
      if (error.code == 902) {
        errorMsg.value = "Sorry, the sharing link you visited does not exist or has expired~";      } else if (error.code == 903) {
        errorMsg.value = "You are late! The shared file has been deleted!";      }
    },
  });

  if (!result) {
    return;
  }

  // 未校验提取码，跳转校验页面
  if (result.data == null) {
    router.push(`/shareCheck/${shareId}`);
    return;
  }

  shareInfo.value = result.data;
};

getShareInfo();

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
  exHeight: 80,
  selectType: "checkbox",
});

// 当前目录
const currentFolder = ref({ fileId: "0" });

// 加载文件列表
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
    shareId: shareId,
    filePid: currentFolder.value.fileId,
  };

  let result = await proxy.Request({
    url: api.loadFileList,
    params: params,
  });

  if (!result) {
    return;
  }

  tableData.value = result.data;
};

const previewRef = ref();
// 点击文件
const clickFile = (data) => {
  // 文件夹点击进入
  if (data.fileClass == 2) {
    navigationRef.value.openFolder(data);
    return;
  }
  data.shareId = shareId;
  previewRef.value.showPreview(data, 2);
};

const navigationRef = ref();
// 导航回调方法
const navChange = (data) => {
  const { currFolder } = data;
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
    selectedIdList.value.push(item.fileId);
  });
};

// 下载文件
const download = async (row) => {
  let result = await proxy.Request({
    url: api.createDownloadCode + "/" + shareId + "/" + row.fileId,
  });

  if (!result) {
    return;
  }

  window.location.href = proxy.globalInfo.downloadUrl + result.data;
};

const folderSelectRef = ref();
const saveFileIdList = ref([]); // 要保存的文件

// 保存文件到我的网盘
const save2MyPan = (row) => {
  // 用户登录则跳转登录页面
  if (!proxy.VueCookies.get("userInfo")) {
    router.push(`/login?redirectUrl=${route.path}`);
    return;
  }

  saveFileIdList.value = [row.fileId];
  folderSelectRef.value.show();
};

// 批量保存文件到我的网盘
const save2MyPanBatch = () => {
  if (selectedIdList.value.length == 0) {
    return;
  }

  // 用户登录则跳转登录页面
  if (!proxy.VueCookies.get("userInfo")) {
    router.push(`/login?redirectUrl=${route.path}`);
    return;
  }

  saveFileIdList.value = selectedIdList.value;
  folderSelectRef.value.show();
};

// save to my network disk
const save2MyPanDone = async (folderId) => {
  let result = await proxy.Request({
    url: api.saveShare,
    params: {
      shareId: shareId,
      shareFileIds: saveFileIdList.value.join(","),
      targetFolderId: folderId,
    },
  });

  if (!result) {
    return;
  }
  proxy.Message.success("Save successfully");
  folderSelectRef.value.close();

  loadDataList();
};

// Cancel sharing
const cancelShare = () => {
  proxy.Confirm("Are you sure you want to cancel sharing?", async () => {
    let result = await proxy.Request({
      url: api.cancelShare,
      params: {
        shareIds: shareId,
      },
    });

    if (!result) {
      return;
    }

    proxy.Message.success("Cancel sharing successfully");
    router.push("/");
  });
};

// Jump to home page
const jump = () => {
  router.push("/");
};
</script>
<style lang="scss" scoped>
@import "@/assets/file_list.scss";

.header {
  width: 100%;
  position: fixed;
  background: #0c95f7;
  height: 50px;

  .header-content {
    width: 70%;
    margin: 0 auto;
    color: #fff;
    line-height: 50px;

    .logo {
      display: flex;
      align-items: center;
      cursor: pointer;

      .icon-pan {
        font-size: 40px;
      }

      .name {
        font-weight: bold;
        margin-left: 5px;
        font-size: 25px;
      }
    }
  }
}

.share-body {
  width: 70%;
  margin: 0 auto;
  padding-top: 50px;

  .share-panel {
    margin-top: 20px;
    display: flex;
    justify-content: space-around;
    border-bottom: 1px solid #ddd;
    padding-bottom: 10px;

    .share-user-info {
      flex: 1;
      display: flex;
      align-items: center;

      .avatar {
        margin-right: 5px;
      }

      .share-info {
        .user-info {
          display: flex;
          align-items: center;

          .nick-name {
            font-size: 15px;
          }

          .share-time {
            margin-left: 20px;
            font-size: 12px;
          }
        }

        .file-name {
          margin-top: 10px;
          font-size: 12px;
        }
      }
    }
  }
}

.file-list {
  margin-top: 10px;

  .file-item {
    .op {
      width: 170px;
    }
  }
}
</style>
