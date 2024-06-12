<!-- Other file preview components -->
<template>
  <div class="others-content">
    <div class="body-content">
      <Icon
          :iconName="fileInfo.fileType == 9 ? 'zip' : 'others'"
          :width="80"
      ></Icon>
      <div class="file-name">{{ fileInfo.fileName }}</div>
      <div class="tips">Preview is not supported for this type of file. Please download and view</div>
      <div class="download-btn">
        <el-button type="primary" @click="download"
        >Click to download{{ proxy.Utils.size2Str(fileInfo.fileSize) }}</el-button
        >
      </div>
    </div>
  </div>
</template>
<script setup>
import { getCurrentInstance } from "vue";

const { proxy } = getCurrentInstance();

const props = defineProps({
  createDownloadUrl: String,
  downloadUrl: String,
  fileInfo: Object,
});

// 下载文件
const download = async () => {
  let result = await proxy.Request({
    url: props.createDownloadUrl,
  });

  if (!result) {
    return;
  }

  window.location.href = props.downloadUrl + result.data;
};
</script>
<style lang="scss" scoped>
.others-content {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;

  .body-content {
    text-align: center;

    .file-name {
      font-weight: bold;
    }

    .tips {
      color: #999898;
      margin-top: 5px;
      font-size: 20px;
    }

    .download-btn {
      margin-top: 20px;
    }
  }
}
</style>
