<!-- 图片文件预览组件 -->
<template>
  <div class="image-viewer">
    <el-image-viewer
      :initial-index="previewImgIndex"
      hide-on-click-modal
      :url-list="imageList"
      @close="close"
      v-if="previewImgIndex != null"
    ></el-image-viewer>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance } from "vue";

const { proxy } = getCurrentInstance();

const props = defineProps({
  imageList: Array,
});
const previewImgIndex = ref(null);

const show = (index) => {
  previewImgIndex.value = index;

  stopScroll();
};
defineExpose({ show });

const close = () => {
  previewImgIndex.value = null;

  startScroll();
};

const stopScroll = () => {
  document.body.style.overflow = "hidden";
};
const startScroll = () => {
  document.body.style.overflow = "auto";
};
</script>
<style lang="scss" scoped>
.image-viewer {
  .el-image-viewer__mask {
    opacity: 0.7;
  }
}
</style>
