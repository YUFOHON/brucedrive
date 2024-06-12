<!-- word文件预览组件，仅支持.docx文件 -->
<template>
  <div ref="wordRef" class="word-content"></div>
</template>
<script setup>
import { ref, getCurrentInstance, onMounted } from "vue";
import * as docx from "docx-preview";

const { proxy } = getCurrentInstance();

const props = defineProps({
  url: String,
});

const wordRef = ref();
const iniWord = async () => {
  let result = await proxy.Request({
    url: props.url,
    responseType: "blob",
  });

  if (!result) {
    return;
  }

  docx.renderAsync(result, wordRef.value);
};

onMounted(() => {
  iniWord();
});
</script>
<style lang="scss" scoped>
.word-content {
  margin: 0 auto;
  :deep(.docx-wrapper) {
    background: #fff;
    padding: 10px 0;
  }

  :deep(.doc-wrapper > section.docx) {
    margin-bottom: 0;
  }
}
</style>
