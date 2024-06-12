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
        ref="formDataRef"
        label-width="80px"
        @submit.prevent
      >
        <el-form-item label="nickname">
          {{ formData.nickName }}
        </el-form-item>
        <el-form-item label="icon">
          <AvatarUpload v-model="formData.avatar"></AvatarUpload>
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance } from "vue";
import AvatarUpload from "../components/AvatarUpload.vue";

const { proxy } = getCurrentInstance();

const api = {
  updateUserAvatar: "/updateUserAvatar",
};

const formData = ref({});
const formDataRef = ref();

const emit = defineEmits();

const dialogConfig = ref({
  show: false,
  title: "Modify avatar",
  buttons: [
    {
      type: "primary",
      text: "OK",
      click: (e) => {
        submitForm();
      },
    },
  ],
});

// 显示修改头像弹窗
const show = (data) => {
  formData.value = Object.assign({}, data);
  formData.value.avatar = { userId: data.userId, qqAvatar: data.avatar };
  dialogConfig.value.show = true;
};

// 向外暴露
defineExpose({ show });

// 提交上传头像
const submitForm = async () => {
  console.log(formData.value.avatar)
  if (!(formData.value.avatar instanceof File)) {
    dialogConfig.value.show = false;
    return;
  }

  // 调用接口修改用户头像
  let result = await proxy.Request({
    url: api.updateUserAvatar,
    params: {
      avatar: formData.value.avatar,
    },
  });
  if (!result) {
    return;
  }

  dialogConfig.value.show = false;

  // 删除缓存中的QQ头像
  const cookieUserInfo = proxy.VueCookies.get("userInfo");
  delete cookieUserInfo.avatar;
  proxy.VueCookies.set("userInfo", cookieUserInfo, 0);

  // 触发updateAvatar事件
  emit("updateAvatar");
};
</script>
