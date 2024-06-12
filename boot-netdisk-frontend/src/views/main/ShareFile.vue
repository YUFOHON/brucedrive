<template>
  <div>
    <Dialog
        :show="dialogConfig.show"
        :title="dialogConfig.title"
        :buttons="dialogConfig.buttons"
        width="600px"
        :showCancel="dialogConfig.showCancel"
        @close="dialogConfig.show = false"
    >
      <el-form
          :model="formData"
          :rules="rules"
          ref="formDataRef"
          label-width="100px"
          @submit.prevent
      >
        <el-form-item label="File">{{ formData.fileName }}</el-form-item>
        <template v-if="showType == 0">
          <el-form-item label="Validity" prop="validType">
            <el-radio-group v-model="formData.validType">
              <el-radio :label="0">1 day</el-radio>
              <el-radio :label="1">7 days</el-radio>
              <el-radio :label="2">30 days</el-radio>
              <el-radio :label="3">Permanently valid</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="Extraction code" prop="codeType">
            <el-radio-group v-model="formData.codeType">
              <el-radio :label="0">Customized</el-radio>
              <el-radio :label="1">System generated</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item prop="code" v-if="formData.codeType == 0">
            <el-input
                clearable
                placeholder="Please enter a 5-digit extraction code"
                v-model="formData.code"
                maxLength="5"
                :style="{ width: '130px' }"
            ></el-input>
          </el-form-item>
        </template>
        <template v-else>
          <el-form-item label="Share link"
          >{{ shareUrl }} {{ resultInfo.shareId }}</el-form-item
          >
          <el-form-item label="Extraction code">{{ resultInfo.code }}</el-form-item>
          <el-form-item>
            <el-button type="primary" @click="copy">Copy link and extraction code</el-button>
          </el-form-item>
        </template>
      </el-form>
    </Dialog>
  </div>
</template>
<script setup>
import { ref, getCurrentInstance, nextTick } from "vue";
import useClipboard from "vue-clipboard3";

const { proxy } = getCurrentInstance();
const { toClipboard } = useClipboard();

const api = {
  shareFile: "/share/shareFile",
};

const formData = ref({});
const formDataRef = ref();
const rules = {
  validType: [{ required: true, message: "Please select the validity period" }],
  codeType: [{ required: true, message: "Please select the extraction code type" }],
  code: [
    { required: true, message: "Please enter the extraction code" },
    { validator: proxy.Valid.shareCode, message: "Please enter the extraction code" },
    { min: 5, message: "The extraction code must be 5 characters long" },
  ],
};

const shareUrl = ref(document.location.origin + "/share/");

// Display type: 0 share form; 1 share result
const showType = ref(0);
const resultInfo = ref({});

const dialogConfig = ref({
  show: false,
  title: "Share",
  buttons: [
    {
      type: "primary",
      text: "Confirm",
      click: (e) => {
        share();
      },
    },
  ],
  showCancel: true,
});

const show = (data) => {
  // reset form
  showType.value = 0;
  dialogConfig.value.showCancel = true;
  dialogConfig.value.show = true;
  resultInfo.value = {};
  dialogConfig.value.buttons[0].text = "Confirm";

  nextTick(() => {
    formDataRef.value.resetFields();
    formData.value = Object.assign({}, data);
  });
};

defineExpose({ show });

// 分享文件
const share = async () => {
  if (Object.keys(resultInfo.value).length > 0) {
    dialogConfig.value.show = false;
    return;
  }

  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    let params = {};
    Object.assign(params, formData.value);

    let result = await proxy.Request({
      url: api.shareFile,
      params: params,
    });

    if (!result) {
      return;
    }

    showType.value = 1;
    resultInfo.value = result.data;
    // 去除关闭按钮，确定按钮改为关闭
    dialogConfig.value.showCancel = false;
    dialogConfig.value.buttons[0].text = "Close";
  });
};

// Copy link and extract code
const copy = async () => {
  const baseUrl = import.meta.env.VITE_BASE_URL || 'brucedrive';

  await toClipboard(
      `Link: ${shareUrl.value}${resultInfo.value.shareId} Extraction code: ${resultInfo.value.code}`
  );
  proxy.Message.success("Copy successful");
};
</script>
