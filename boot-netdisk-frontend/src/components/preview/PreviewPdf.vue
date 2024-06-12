<!-- pdf文件预览组件 -->
<template>
  <div class="pdf-content">
    <vue-pdf-embed
      ref="psfRef"
      :source="state.url"
      class="vue-pdf-embed"
      width="850"
      :page="state.pageNum"
    ></vue-pdf-embed>
  </div>
</template>
<script setup>
import { ref, onMounted } from "vue";
import VuePdfEmbed from "vue-pdf-embed";

const props = defineProps({
  url: String,
});

const state = ref({
  url: "",
  pageNum: 0,
  numPages: 0,
});

const iniPdf = async () => {
  state.value.url = "/api" + props.url;
};

onMounted(() => {
  iniPdf();
});
</script>
<style lang="scss" scoped>
.pdf-content {
  margin: 0 auto;
}
</style>
