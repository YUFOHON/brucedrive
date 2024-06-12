<template>
  <div class="window" v-if="show">
    <div class="window-mask" @click="close" v-if="show"></div>
    <div class="close" @click="close">
      <span class="iconfont icon-close2"></span>
    </div>
    <div
      class="window-content"
      :style="{
        top: '0px',
        left: windowContentLeft + 'px',
        width: windowContentWidth + 'px',
      }"
    >
      <div class="title">{{ title }}</div>
      <div class="content-body" :style="{ 'align-items': align }">
        <slot></slot>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";

const props = defineProps({
  show: Boolean,
  width: {
    type: Number,
    default: 1000,
  },
  title: String,
  align: {
    type: String,
    default: "top",
  },
});

// 计算窗体大小
const windowWidth = ref(window.innerWidth);
const windowContentWidth = computed(() => {
  return props.width > windowWidth.value ? windowWidth.value : props.width;
});
const windowContentLeft = computed(() => {
  let left = windowWidth.value - props.width;
  return left < 0 ? 0 : left / 2;
});

const emit = defineEmits(["close"]);
// 关闭窗体
const close = () => {
  emit("close");
};

// 重置窗体大小
const resize = () => {
  windowWidth.value = window.innerWidth;
};

onMounted(() => {
  window.addEventListener("resize", resize);
});
onUnmounted(() => {
  window.removeEventListener("resize", resize);
});
</script>
<style lang="scss" scoped>
.window {
  .window-mask {
    top: 0;
    left: 0;
    width: 100%;
    height: calc(100vh);
    z-index: 200;
    opacity: 0.5;
    background: #000;
    position: fixed;
  }

  .close {
    z-index: 202;
    cursor: pointer;
    position: absolute;
    top: 40px;
    right: 30px;
    width: 44px;
    height: 44px;
    border-radius: 22px;
    background: #606266;
    display: flex;
    justify-content: center;
    align-items: center;

    .iconfont {
      font-size: 20px;
      color: #800080;
      z-index: 100000;
    }
  }

  .window-content {
    top: 0;
    z-index: 201;
    position: absolute;
    background: #fff;

    .title {
      text-align: center;
      line-height: 40px;
      border-bottom: 1px solid #ddd;
      font-weight: bold;
    }

    .content-body {
      height: calc(100vh - 41px);
      display: flex;
      overflow: auto;
    }
  }
}
</style>
