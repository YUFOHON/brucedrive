<!-- 头像上传组件 -->
<template>
  <div class="avatar-upload">
    <div class="avatar-show">
      <template v-if="localFile">
        <img :src="localFile" />
      </template>
      <template v-else>
        <img
          :src="`${modelValue.qqAvatar}`"
          v-if="modelValue && modelValue.qqAvatar"
        />
        <img
          :src="`${proxy.globalInfo.avatarUrl}${modelValue.userId}`"
          v-else
        />
      </template>
    </div>
    <div class="select-btn">
      <el-upload
        name="file"
        :show-file-list="false"
        accept=".png,.jpg,.jpeg,.gif,.bmp"
        :multiple="false"
        :http-request="uploadImage"
      >
        <el-button type="primary">confirm</el-button>
      </el-upload>
    </div>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance } from "vue";

const { proxy } = getCurrentInstance();

const props = defineProps({
  modelValue: {
    type: Object,
    default: null,
  },
});

const localFile = ref(null);
const emit = defineEmits();

// 选择图片上传
const uploadImage = async (file) => {
  // 读取本地文件 base64
  file = file.file;
  let img = new FileReader();
  img.readAsDataURL(file);
  img.onload = ({ target }) => {
    localFile.value = target.result;
  };

  // 双向绑定v-model
  emit("update:modelValue", file);
};
</script>
<style lang="scss" scoped>
.avatar-upload {
  display: flex;
  justify-content: center;
  align-items: end;

  .avatar-show {
    background: rgb(245, 245, 245);
    width: 150px;
    height: 150px;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    position: relative;

    .iconfont {
      font-size: 50px;
      color: #ddd;
    }
    img {
      width: 100%;
      height: 100%;
    }
    .op {
      position: absolute;
      color: #0e8aef;
      top: 80px;
    }
  }
  .select-btn {
    margin-left: 10px;
    vertical-align: bottom;
  }
}
</style>
