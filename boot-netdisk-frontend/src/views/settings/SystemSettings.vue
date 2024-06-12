<template>
  <div>
    <div class="sys-setting-panel">
      <el-form
          :model="formData"
          :rules="rules"
          ref="formDataRef"
          label-width="150px"
          @submit.prevent
      >
        <el-form-item label="Registration Email" prop="registerEmailTitle">
          <el-input
              clearable
              placeholder="Please enter the registration email verification code email title"
              v-model.trim="formData.registerEmailTitle"
          ></el-input
          ></el-form-item>
        <el-form-item label="Email Content" prop="registerEmailContent">
          <el-input
              clearable
              placeholder="Please enter the registration email verification code email content"
              v-model.trim="formData.registerEmailContent"
          ></el-input>
        </el-form-item>
        <el-form-item label="Initial space" prop="initUserSpace">
          <el-input
              clearable
              placeholder="Please enter the initial space size of the user's network disk"
              v-model.trim="formData.initUserSpace"
              type="number"
          ><template #suffix>MB</template></el-input
          >
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveSettings">Save</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance, nextTick } from "vue";

const { proxy } = getCurrentInstance();

const api = {
  getSystemSettings: "/admin/getSystemSettings",
  saveSystemSettings: "/admin/saveSystemSettings",
};

const formData = ref({});
const formDataRef = ref();
const rules = {
  registerEmailTitle: [
    { required: true, message: "Please enter the title of the registration email verification code" },
  ],
  registerEmailContent: [
    { required: true, message: "Please enter the content of the registration email verification code" },
  ],
  initUserSpace: [
    { required: true, message: "Please enter the initialization space size of the user network disk" },
    { validator: proxy.Valid.number, message: "The space size can only be a number" },
  ],
};
// 获取系统配置信息
const getSettings = async () => {
  let result = await proxy.Request({
    url: api.getSystemSettings,
  });

  if (!result) {
    return;
  }

  formData.value = result.data;
};

getSettings();

const saveSettings = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    let params = {};
    Object.assign(params, formData.value);
    let result = await proxy.Request({
      url: api.saveSystemSettings,
      params: params,
    });

    if (!result) {
      return;
    }
    proxy.Message.success("Saved successfully");
  });
};
</script>
<style lang="scss" scoped>
.sys-setting-panel {
  margin-top: 20px;
  width: 600px;
}
</style>
