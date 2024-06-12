<!-- 音频文件预览组件 -->
<template>
  <div class="audio-content">
    <div class="body-content">
      <div class="cover">
        <img src="@/assets/images/music_cover.png" />
      </div>
      <div ref="audioRef" class="audio-player"></div>
    </div>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance, onMounted, onUnmounted } from "vue";
import APlayer from "APlayer";
import "Aplayer/dist/Aplayer.min.css";

const { proxy } = getCurrentInstance();

const props = defineProps({
  url: String,
  fileName: String,
});

const audioRef = ref();
const player = ref();

onMounted(() => {
  player.value = new APlayer({
    container: audioRef.value,
    audio: {
      url: `/api/${props.url}`,
      name: props.fileName,
      cover: new URL("@/assets/icon/music.png", import.meta.url).href,
    },
  });
});

onUnmounted(() => {
  player.value.destroy();
});
</script>
<style lang="scss" scoped>
.audio-content {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;

  .body-content {
    text-align: center;
    width: 80%;

    .cover {
      margin: 0 auto;
      width: 50%;
      text-align: center;

      img {
        width: 100%;
      }
    }

    .audio-player {
      margin-top: 20px;
    }
  }
}
</style>
