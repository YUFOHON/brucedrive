<!-- txt文件预览组件 -->
<template>
  <div class="txt-content">
    <div class="top-op">
      <div class="encode-select">
        <el-select
          clearable
          placeholder="选择编码"
          v-model="encode"
          @change="changeEncode"
        >
          <el-option value="utf8" label="utf8编码"></el-option>
          <el-option value="gbk" label="gbk编码"></el-option>
        </el-select>
        <div class="tips">乱码了？切换编码</div>
      </div>
      <div class="copy-btn">
        <el-button type="primary" @click="copy">复制</el-button>
      </div>
    </div>
    <highlightjs autodetect :code="txtContent"></highlightjs>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance, onMounted } from "vue";
import useClipboard from "vue-clipboard3";

const { proxy } = getCurrentInstance();
const { toClipboard } = useClipboard();

const props = defineProps({
  url: String,
});

const txtContent = ref("");
const blobResult = ref(); // 文件内容
const encode = ref("utf8"); // 文件编码

// 读取文档
const readTxt = async () => {
  // 获取文件内容
  let result = await proxy.Request({
    url: props.url,
    responseType: "blob",
  });

  if (!result) {
    return;
  }

  blobResult.value = result;

  showTxt();
};

// 切换编码
const changeEncode = (e) => {
  encode.value = e;
  showTxt();
};

// 显示文本内容
const showTxt = () => {
  const reader = new FileReader();
  reader.onload = () => {
    txtContent.value = reader.result;
  };
  reader.readAsText(blobResult.value, encode.value);
};

// 复制内容
const copy = async () => {
  await toClipboard(txtContent.value);
  proxy.Message.success("复制成功");
};

onMounted(() => {
  readTxt();
});
</script>
<style lang="scss" scoped>
.txt-content {
  width: 100%;

  .top-op {
    display: flex;
    align-items: center;
    justify-content: space-around;
  }

  .encode-select {
    flex: 1;
    display: flex;
    align-items: center;
    margin: 5px 10px;

    .tips {
      margin-left: 10px;
      color: #828282;
    }
  }

  .copy-btn {
    margin-right: 10px;
  }

  pre {
    margin: 0;
  }
}
</style>
