<template>
  <el-config-provider :locale="locale" :message="config">
    <router-view></router-view>
  </el-config-provider>
</template>
<style lang="scss" scoped></style>
<script setup>
  import { reactive } from 'vue';
  //import zhCn from "element-plus/dist/locale/zh-cn.mjs";
  import en from "element-plus/dist/locale/en.mjs";
  const locale = en;
  const config = reactive({
    max: 1,
  });
</script>