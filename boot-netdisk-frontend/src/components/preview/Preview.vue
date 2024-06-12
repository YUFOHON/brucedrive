<!-- 文件预览组件 -->
<template>
  <div>
    <!-- 图片类型预览 -->
    <PreviewImage
      ref="imageViewRef"
      :imageList="[imageUrl]"
      v-if="fileInfo.fileCategory == 3"
    ></PreviewImage>

    <!-- 其他类型预览窗体 -->
    <Window
      :show="windowShow"
      @close="closeWindow"
      :width="fileInfo.fileCategory == 1 ? 1500 : 900"
      :title="fileInfo.fileName"
      :align="fileInfo.fileCategory == 1 ? 'center' : 'top'"
      v-else
    >
      <!-- 视频类型预览 -->
      <PreviewVideo :url="url" v-if="fileInfo.fileCategory == 1"></PreviewVideo>

      <!-- 音频类型预览 -->
      <PreviewAudio
        :url="url"
        :fileName="fileInfo.fileName"
        v-if="fileInfo.fileCategory == 2"
      ></PreviewAudio>

      <!-- pdf类型预览 -->
      <PreviewPdf :url="url" v-if="fileInfo.fileType == 4"></PreviewPdf>

      <!-- word类型预览，仅支持.docx文件，后端将.doc转换为.docx -->
      <PreviewWord :url="url" v-if="fileInfo.fileType == 5"></PreviewWord>

      <!-- excel类型预览 -->
      <PreviewExcel :url="url" v-if="fileInfo.fileType == 6"></PreviewExcel>

      <!-- txt类型预览，txt和程序代码 -->
      <PreviewTxt
        :url="url"
        v-if="fileInfo.fileType == 7 || fileInfo.fileType == 8"
      ></PreviewTxt>

      <PreviewOthers
        :createDownloadUrl="createDownloadUrl"
        :downloadUrl="downloadUrl"
        :fileInfo="fileInfo"
        v-if="fileInfo.fileCategory == 5"
      ></PreviewOthers>
    </Window>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance, computed, nextTick } from "vue";
import PreviewImage from "./PreviewImage.vue";
import PreviewVideo from "./PreviewVideo.vue";
import PreviewAudio from "./PreviewAudio.vue";
import PreviewWord from "./PreviewWord.vue";
import PreviewExcel from "./PreviewExcel.vue";
import PreviewPdf from "./PreviewPdf.vue";
import PreviewTxt from "./PreviewTxt.vue";
import PreviewOthers from "./PreviewOthers.vue";

const { proxy } = getCurrentInstance();

const api = {
  0: {
    fileUrl: "/file/getFile",
    videoUrl: "file/getVideoFile",
    createDownloadUrl: "/file/createDownloadCode",
  },
  1: {
    fileUrl: "/admin/getFile",
    videoUrl: "admin/getVideoFile",
    createDownloadUrl: "/admin/createDownloadCode",
  },
  2: {
    fileUrl: "/webShare/getFile",
    videoUrl: "webShare/getVideoFile",
    createDownloadUrl: "/webShare/createDownloadCode",
  },
};

// 图片预览
const imageViewRef = ref();
const imageUrl = computed(() => {
  // 缩略图替换为原图
  return (
    proxy.globalInfo.imageUrl + fileInfo.value.fileCover.replaceAll("_.", ".")
  );
});

// 显示窗体
const windowShow = ref(false);
// 关闭窗体
const closeWindow = () => {
  windowShow.value = false;
};

const url = ref(null);
const createDownloadUrl = ref(null);
const downloadUrl = ref(null);

// 预览文件信息
const fileInfo = ref({});

// 显示预览组件
const showPreview = (data, showPart) => {
  fileInfo.value = data;
  if (data.fileCategory == 3) {
    nextTick(() => {
      imageViewRef.value.show(0);
    });
  } else {
    windowShow.value = true;

    // 文件链接
    let _url = api[showPart].fileUrl;
    let _createDownloadUrl = api[showPart].createDownloadUrl;

    // 视频链接
    if (data.fileCategory == 1) {
      _url = api[showPart].videoUrl;
    }

    // 用户自己文件预览下载
    if (showPart == 0) {
      _url = _url + "/" + data.fileId;
      _createDownloadUrl = _createDownloadUrl + "/" + data.fileId;
    } else if (showPart == 1) {
      // 管理员文件预览下载
      _url = _url + "/" + data.userId + "/" + data.fileId;
      _createDownloadUrl =
        _createDownloadUrl + "/" + data.userId + "/" + data.fileId;
    } else if (showPart == 2) {
      // 分享文件预览下载
      _url = _url + "/" + data.shareId + "/" + data.fileId;
      _createDownloadUrl =
        _createDownloadUrl + "/" + data.shareId + "/" + data.fileId;
    }

    url.value = _url;
    createDownloadUrl.value = _createDownloadUrl;
    downloadUrl.value = proxy.globalInfo.downloadUrl;
  }
};
defineExpose({ showPreview });
</script>
