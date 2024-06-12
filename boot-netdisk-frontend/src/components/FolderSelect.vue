<!-- 文件夹选择框组件 -->
<template>
  <div>
    <Dialog
      :show="dialogConfig.show"
      :title="dialogConfig.title"
      :buttons="dialogConfig.buttons"
      width="400px"
      :showCancel="false"
      @close="dialogConfig.show = false"
    >
      <div class="navigation-panel">
        <Navigation
          ref="navigationRef"
          @navChange="navChange"
          :watchPath="false"
        ></Navigation>
      </div>
      <div class="folder-list" v-if="folderList.length > 0">
        <div
          class="folder-item"
          v-for="item in folderList"
          @click="selectFolder(item)"
        >
          <Icon :fileType="0"></Icon>
          <span class="file-item">{{ item.fileName }}</span>
        </div>
      </div>
      <div v-else class="tips">
        Move to <span>{{ currentFolder.fileName }}</span> folder
      </div>
    </Dialog>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance } from "vue";

const { proxy } = getCurrentInstance();

const api = {
  loadFolderList: "/file/loadFolderList",
};

const dialogConfig = ref({
  show: false,
  title: "Move to",
  buttons: [
    {
      type: "primary",
      text: "Move here",
      click: (e) => {
        folderSelect();
      },
    },
  ],
});

const filePid = ref("0"); // 父级ID
const folderList = ref([]);
const currentFileIds = ref({});
const currentFolder = ref({});

// 加载目录
const loadFolderList = async () => {
  let result = await proxy.Request({
    url: api.loadFolderList,
    params: {
      filePid: filePid.value,
      currFileIds: currentFileIds.value,
    },
  });

  if (!result) {
    return;
  }

  folderList.value = result.data;
};

// 显示弹窗
const show = (currFolder) => {
  currentFileIds.value = currFolder;
  dialogConfig.value.show = true;
  loadFolderList();
};

// 关闭弹窗
const close = () => {
  dialogConfig.value.show = false;
};

defineExpose({ show, close });

const navigationRef = ref();
// 选择目录
const selectFolder = (data) => {
  navigationRef.value.openFolder(data);
};

const emit = defineEmits(["folderSelect"]);
// 确定选中目录
const folderSelect = () => {
  emit("folderSelect", filePid.value);
};

// 导航回调方法
const navChange = (data) => {
  const { currFolder } = data;
  currentFolder.value = currFolder;
  filePid.value = currFolder.fileId;

  loadFolderList();
};
</script>
<style lang="scss" scoped>
.navigation-panel {
  padding-left: 10px;
  background: #f1f1f1;
}
.folder-list {
  max-height: calc(100vh - 200px);
  min-height: 200px;

  .folder-item {
    cursor: pointer;
    display: flex;
    align-items: center;
    padding: 10px;

    .file-name {
      display: inline-block;
      margin-left: 10px;
    }
    &:hover {
      background: #f8f8f8;
    }
  }
}

.tips {
  text-align: center;
  line-height: 200px;

  span {
    color: #06a7ff;
  }
}
</style>
