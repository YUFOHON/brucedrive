<!-- excel文件预览组件 -->
<template>
  <div v-html="excelContent" class="excel-content"></div>
</template>
<script setup>
import { ref, getCurrentInstance, onMounted } from "vue";
import * as xlsx from "xlsx";

const { proxy } = getCurrentInstance();

const props = defineProps({
  url: String,
});

const excelContent = ref();

const iniExcel = async () => {
  let result = await proxy.Request({
    url: props.url,
    responseType: "arraybuffer",
  });

  if (!result) {
    return;
  }

  let workbook = xlsx.read(new Uint8Array(result), { type: "array" });
  let worksheet = workbook.Sheets[workbook.SheetNames[0]];
  excelContent.value = xlsx.utils.sheet_to_html(worksheet);
};

onMounted(() => {
  iniExcel();
});
</script>
<style lang="scss" scoped>
.excel-content {
  width: 100%;
  padding: 10px;

  :deep(table) {
    width: 100%;
    border-collapse: collapse;

    td {
      border: 1px solid #ddd;
      border-collapse: collapse;
      padding: 5px;
      height: 30px;
      min-height: 50px;
    }
  }
}
</style>
