<!-- 头像组件 -->
<template>
  <span class="avatar" :style="{ width: width + 'px', height: width + 'px' }">
    <!-- QQ登录时返回头像avatar，账号登录时根据userId从接口获取 -->
    <img
      :src="
        avatar && avatar != ''
          ? avatar
          : `${proxy.globalInfo.avatarUrl}${userId}?${timestamp}`
      "
      v-if="userId"
      @error.once="loadDefaultAvatar"
    />
  </span>
</template>
<script setup>
import { getCurrentInstance } from "vue";

const { proxy } = getCurrentInstance();

const props = defineProps({
  userId: {
    type: String,
  },
  avatar: {
    type: String,
  },
  timestamp: {
    type: Number,
    default: 0,
  },
  width: {
    type: Number,
    default: 40,
  },
});

// 加载头像失败
const loadDefaultAvatar = (e) => {
  // 加载默认头像
  e.target.src = new URL(
    "@/assets/images/default_avatar.jpg",
    import.meta.url
  ).href;
};
</script>
<style lang="scss" scoped>
.avatar {
  display: flex;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  img {
    width: 100%;
    object-fit: cover;
  }
}
</style>
