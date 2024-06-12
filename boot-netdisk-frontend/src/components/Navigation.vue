<!-- file navigation -->
<template>
  <div class="top-navigation">
    <template v-if="folderList.length > 0">
      <span class="back link" @click="backParent">Back to the previous level</span>
      <el-divider direction="vertical"></el-divider>
    </template>
    <span v-if="folderList.length == 0" class="all-file">All files</span>
    <span v-if="folderList.length > 0" class="link" @click="setCurrFolder(-1)"
    >All files</span
    >
    <template v-for="(item, index) in folderList">
      <span class="iconfont icon-right"></span>
      <span
          class="link"
          v-if="index < folderList.length - 1"
          @click="setCurrFolder(index)"
      >{{ item.fileName }}</span
      >
      <span class="text" v-if="index == folderList.length - 1">{{
          item.fileName
        }}</span>
    </template>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance, watch } from "vue";
import { useRoute, useRouter } from "vue-router";

const { proxy } = getCurrentInstance();
const route = useRoute();
const router = useRouter();

const props = defineProps({
  watchPath: {
    type: Boolean,
    default: true,
  },
  shareId: String,
  adminShow: {
    type: Boolean,
    default: false,
  },
});

const api = {
  getFolderList: "/file/getFolderList",
  getFolderListShare: "/webShare/getFolderList",
  getFolderListAdmin: "/admin/getFolderList",
};

const category = ref(); // 分类
const folderList = ref([]); // 目录集合
const currFolder = ref({ fileId: "0" }); // 当前目录

// 初始化导航目录
const init = () => {
  folderList.value = [];
  currFolder.value = { fileId: "0" };
};

// 点击文件夹打来目录
const openFolder = (data) => {
  const { fileId, fileName } = data;
  const folder = {
    fileId: fileId,
    fileName: fileName,
  };
  folderList.value.push(folder);
  currFolder.value = folder;

  setPath();
};

defineExpose({ openFolder });

// 设置导航路径
const setPath = () => {
  // 不监听路由则直接执行回调
  if (!props.watchPath) {
    doCallback();
    return;
  }

  // 读取导航路径
  let pathArr = [];
  folderList.value.forEach((item) => {
    pathArr.push(item.fileId);
  });

  // 设置路由
  router.push({
    path: route.path,
    query: pathArr.length == 0 ? "" : { path: pathArr.join("/") },
  });
};

// 获取当前路径的目录
const getNavgationFolder = async (path) => {
  let url = api.getFolderList;
  if (props.shareId) {
    url = api.getFolderListShare;
  }
  if (props.adminShow) {
    url = api.getFolderListAdmin;
  }

  let result = await proxy.Request({
    url: url,
    showLoading: false,
    params: {
      path: path,
      shareId: props.shareId,
    },
  });

  if (!result) {
    return;
  }

  folderList.value = result.data;
};

// 点击导航，跳转目录
const setCurrFolder = (index) => {
  // 点击全部文件，初始化导航目录
  if (index == -1) {
    init();
  } else {
    currFolder.value = folderList.value[index];
    // 删除后面的导航目录
    folderList.value.splice(index + 1, folderList.value.length);
  }

  setPath();
};

// 返回上一级目录
const backParent = () => {
  // 循环遍历查找当前目录的索引
  let currIndex = null;
  for (let i = 0; i < folderList.value.length; i++) {
    if (folderList.value[i].fileId == currFolder.value.fileId) {
      currIndex = i;
      break;
    }
  }
  // 跳转到上一个目录
  setCurrFolder(currIndex - 1);
};

// 导航发生改变，调用main页面的回调方法
const emit = defineEmits(["navChange"]);
const doCallback = () => {
  emit("navChange", {
    categoryId: category.value,
    currFolder: currFolder.value,
  });
};

watch(
  () => route,
  (newVal, oldVal) => {
    // 不监听路由则不往下执行
    if (!props.watchPath) {
      return;
    }

    // 不走导航的页面：除了主页、文件列表、分享页面以外
    if (
      newVal.path.indexOf("/main") === -1 &&
      newVal.path.indexOf("/fileList") === -1 &&
      newVal.path.indexOf("/share") === -1
    ) {
      return;
    }

    const path = newVal.query.path;
    category.value = newVal.params.category;

    // 刷新页面
    if (path == undefined) {
      init();
    } else {
      // 路由跳转
      getNavgationFolder(path);

      let pathArr = path.split("/");
      currFolder.value = {
        fileId: pathArr[pathArr.length - 1],
      };
    }

    doCallback();
  },
  { immediate: true, deep: true }
);
</script>
<style lang="scss" scoped>
.top-navigation {
  font-size: 13px;
  display: flex;
  align-items: center;
  line-height: 40px;

  .all-file {
    font-weight: bold;
  }

  .link {
    color: #06a7ff;
    cursor: pointer;
  }

  .icon-right {
    color: #06a7ff;
    padding: 0 5px;
    font-size: 13px;
  }
}
</style>
