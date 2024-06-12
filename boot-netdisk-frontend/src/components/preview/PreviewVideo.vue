<!-- 视频文件预览组件 -->
<template>
  <div ref="videoRef" id="player"></div>
</template>
<script setup>
import { onMounted, ref } from "vue";
import DPlayer from "dplayer";

const props = defineProps({
  url: String,
});

const videoRef = ref();
const initPlayer = () => {
  const dp = new DPlayer({
    element: videoRef.value,
    theme: "#b7daff",
    screenshot: true,
    video: {
      url: `/api/${props.url}`,
      type: "customHls",
      customType: {
        customHls: function (video, player) {
          const hls = new Hls();
          hls.loadSource(video.src);
          hls.attachMedia(video);
        },
      },
    },
  });
};

onMounted(() => {
  initPlayer();
});
</script>
<style lang="scss" scoped>
#player {
  width: 100%;
  
  :deep(.dplayer-video-wrap) {
    text-align: center;

    .dplayer-video {
      margin: 0 auto;
      max-height: calc(100vh - 41px);
    }
  }
}
</style>
