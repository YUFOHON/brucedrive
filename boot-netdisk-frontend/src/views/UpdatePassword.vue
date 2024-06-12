<template>
  <div>
    <Dialog
      :show="dialogConfig.show"
      :title="dialogConfig.title"
      :buttons="dialogConfig.buttons"
      width="500px"
      :showCancel="false"
      @close="dialogConfig.show = false"
    >
      <el-form
        :model="formData"
        :rules="rules"
        ref="formDataRef"
        label-width="80px"
        @submit.prevent
      >
        <el-form-item label="new password" prop="password">
          <el-input
            type="password"
            size="large"
            placeholder="Please enter a password"
            v-model.trim="formData.password"
            show-password
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="confirm password" prop="rePassword">
          <el-input
            type="password"
            size="large"
            placeholder="please enter the password again"
            v-model.trim="formData.rePassword"
            show-password
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance, nextTick } from "vue";

const { proxy } = getCurrentInstance();

const api = {
  updatePassword: "/updatePassword",
};

const formData = ref({});
const formDataRef = ref();

const emit = defineEmits();

const dialogConfig = ref({
  show: false,
  title: "change password",
  buttons: [
    {
      type: "primary",
      text: "confirm",
      click: (e) => {
        submitForm();
      },
    },
  ],
});

// 两次密码校验
const checkRePassword = (rule, value, callback) => {
  if (value != formData.value.password) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
};

// 参数校验规则
const rules = {
  password: [
    { required: true, message: "Please enter your password" },
    {
      validator: proxy.Valid.password,
      message: "The password must contain numbers, letters and special characters, and be at least 8 characters long",
    },
  ],
  rePassword: [
    { required: true, message: "Please enter your password" },
    {
      validator: checkRePassword,
      message: "The passwords entered twice are inconsistent",
    },
  ],
};

const show = () => {
  dialogConfig.value.show = true;
  nextTick(() => {
    formDataRef.value.resetFields();
    formData.value = {};
  });
};

defineExpose({ show });

const submitForm = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    let result = await proxy.Request({
      url: api.updatePassword,
      params: {
        password: formData.value.password,
      },
    });

    if (!result) {
      return;
    }

    dialogConfig.value.show = false;
    proxy.Message.success("password changed");
  });
};
</script>
<style lang="scss" scoped></style>
