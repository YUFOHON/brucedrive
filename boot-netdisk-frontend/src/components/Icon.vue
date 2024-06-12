<!-- 图标组件 -->
<template>
  <span :style="{ width: width + 'px', height: width + 'px' }" class="icon">
    <img :src="getImage()" :style="{ 'object-fit': fit }" />
  </span>
</template>
<script setup>
import { ref, reactive, getCurrentInstance } from "vue";

const { proxy } = getCurrentInstance();

const props = defineProps({
  fileType: Number,
  iconName: String,
  cover: String,
  width: {
    type: Number,
    default: 32,
  },
  fit: {
    type: String,
    default: "cover",
  },
});

const filtTypeMap = {
  0: { icon: "folder", desc: "目录" },
  1: { icon: "video", desc: "视频" },
  2: { icon: "audio", desc: "音频" },
  3: { icon: "image", desc: "图片" },
  4: { icon: "pdf", desc: "pdf" },
  5: { icon: "word", desc: "doc" },
  6: { icon: "excel", desc: "excel" },
  7: { icon: "txt", desc: "txt文本" },
  8: { icon: "code", desc: "程序" },
  9: { icon: "zip", desc: "压缩文件" },
  10: { icon: "others", desc: "其他" },
};

// 获取图标
const getImage = () => {
  // 封面图片
  if (props.cover) {
    return proxy.globalInfo.imageUrl + props.cover;
  }

  let icon = "unknow_icon";
  // 根据图标文件名称
  if (props.iconName) {
    icon = props.iconName;
  } else {
    // 根据文件类型匹配图标
    const iconMap = filtTypeMap[props.fileType];
    if (iconMap != undefined) {
      icon = iconMap["icon"];
    }
  }
  return new URL(`/src/assets/icon/${icon}.png`, import.meta.url).href;
};
</script>
<style lang="scss" scoped>
.icon {
  text-align: center;
  display: inline-block;
  border-radius: 3px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
  }
}
</style>
