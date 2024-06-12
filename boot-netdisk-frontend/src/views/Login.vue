<template>
  <div class="login-body">
    <div class="bg"></div>
    <div class="login-panel">
      <el-form
          class="login-register"
          :model="formData"
          :rules="rules"
          ref="formDataRef"
          @submit.prevent
      >
        <div class="login-title">Bruce Drive</div>
        <!-- Email -->
        <el-form-item prop="email">
          <el-input
              size="large"
              clearable
              placeholder="Please enter your email"
              v-model.trim="formData.email"
              maxLength="150"
          >
            <template #prefix>
              <span class="iconfont icon-account"></span>
            </template>
          </el-input>
        </el-form-item>

        <!-- Display content when registering or resetting password -->
        <div v-if="opType == 0 || opType == 2">
          <!-- Email verification code -->
          <el-form-item prop="emailCode">
            <div class="send-email-panel">
              <el-input
                  size="large"
                  placeholder="Please enter the email verification code"
                  v-model.trim="formData.emailCode"
              >
                <template #prefix>
                  <span class="iconfont icon-checkcode"></span>
                </template>
              </el-input>
              <el-button
                  class="send-email-btn"
                  type="primary"
                  size="large"
                  @click="showSendEmailCodeDialog"
              >Get verification code
              </el-button
              >
            </div>
            <el-popover placement="left" :width="300" trigger="click">
              <div>
                <p>1. Find the email verification code in the spam mailbox</p>
                <p>2. Add the email address [xxx.qq.com] to the whitelist</p>
                <p>3. Contact customer service</p>
              </div>
              <template #reference>
<span class="a-link" :style="{ 'font-size': '14px' }"
>Didn't receive the email verification code? </span
>
              </template>
            </el-popover>
          </el-form-item>

          <!-- Nickname, only displayed when registering -->
          <el-form-item prop="nickName" v-if="opType == 0">
            <el-input
                size="large"
                placeholder="Please enter a nickname"
                v-model.trim="formData.nickName"
                maxLength="20"
            >
              <template #prefix>
                <span class="iconfont icon-account"></span>
              </template>
            </el-input>
          </el-form-item>

          <!-- New password, verification rules are different from login password -->
          <el-form-item prop="newPassword">
            <el-input
                type="password"
                size="large"
                placeholder="Please enter a password"
                v-model.trim="formData.newPassword"
                show-password
            >
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>

          <!-- Repeat password -->
          <el-form-item prop="rePassword">
            <el-input
                type="password"
                size="large"
                placeholder="Please enter your password again"
                v-model.trim="formData.rePassword"
                show-password
            >
              <template #prefix>
                <span class="iconfont icon-password"></span>
              </template>
            </el-input>
          </el-form-item>
        </div>

        <!-- Login password, displayed when logging in -->
        <el-form-item prop="password" v-if="opType == 1">
          <el-input
              type="password"
              size="large"
              placeholder="Please enter your password"
              v-model.trim="formData.password"
              show-password
          >
            <template #prefix>
              <span class="iconfont icon-password"></span>
            </template>
          </el-input>
        </el-form-item>

        <!-- Verification code -->
        <el-form-item prop="captchaCode">
          <div class="captcha-code-panel">
            <el-input
                size="large"
                placeholder="Please enter your verification code"
                v-model.trim="formData.captchaCode"
                @keyup.enter="doSubmit"
            >
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img
                :src="getCaptchaCheckUrl"
                alt="Verification code"
                class="captcha-code"
                @click="refreshCaptcha(0)"
            />
          </div>
        </el-form-item>

        <!-- Already have an account, display when registering -->
        <el-form-item v-if="opType == 0">
          <a href="javascript:void(0)" class="a-link" @click="switchPanel(1)"
          > Already have an account? </a
          >
        </el-form-item>

        <!-- Remember me, display when logging in -->
        <el-form-item v-if="opType == 1">
          <div class="rememberme-panel">
            <el-checkbox v-model="formData.rememberMe">Remember me</el-checkbox>
          </div>
          <div class="no-account">
            <a href="javascript:void(0)" class="a-link" @click="switchPanel(2)"
            > Forgot your password? </a
            >
            <a href="javascript:void(0)" class="a-link" @click="switchPanel(0)"
            > No account? </a
            >
          </div>
        </el-form-item>

        <!-- Go to login, displayed when retrieving password -->
        <el-form-item v-if="opType == 2">
          <a href="javascript:void(0)" class="a-link" @click="switchPanel(1)"
          >Go to login? </a
          >
        </el-form-item>
        <!-- Login button -->
        <el-form-item>
          <el-button
              type="primary"
              class="op-btn"
              size="large"
              @click="doSubmit"
          >
            <span v-if="opType == 0">Register</span>
            <span v-if="opType == 1">Login</span>
            <span v-if="opType == 2">Reset password</span>
          </el-button>
        </el-form-item>
        <div class="login-btn-qq" v-if="opType == 1">
<!--          Quick login<img src="@/assets/icon/qq.png" @click="qqLogin"/>-->
        </div>
      </el-form>
    </div>

    <!-- Send email verification code pop-up window -->
    <Dialog
        :show="smcDialogConfig.show"
        :title="smcDialogConfig.title"
        :buttons="smcDialogConfig.buttons"
        :showCancel="false"
        width="500px"
        @close="smcDialogConfig.show = false"
    >
      <el-form
          :model="smcFormData"
          :rules="rules"
          ref="smcFormDataRef"
          label-width="80px"
          @submit.prevent
      >
        <el-form-item label="Email: ">{{ formData.email }}</el-form-item>
        <el-form-item label="Code: " prop="captchaCode">
          <div class="captcha-code-panel">
            <el-input
                size="large"
                placeholder="Please enter the verification code"
                v-model="smcFormData.captchaCode"
            >
              <template #prefix>
                <span class="iconfont icon-checkcode"></span>
              </template>
            </el-input>
            <img
                :src="getCaptchaEmailUrl"
                class="captcha-code"
                @click="refreshCaptcha(1)"
            />
          </div>
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>
<script setup>
import {ref, reactive, getCurrentInstance, nextTick, onMounted} from "vue";
import {useRouter, useRoute} from "vue-router";
import md5 from "js-md5";

const {proxy} = getCurrentInstance();
const router = useRouter();
const route = useRoute();

const api = {
  sendEmailCode: "/sendEmailCode",
  register: "/register",
  login: "/login",
  resetPwd: "/resetPwd",
  qqLogin: "/qqLogin",
};

const formData = ref({});
const formDataRef = ref();

// 两次密码校验
const checkRePassword = (rule, value, callback) => {
  if (value != formData.value.newPassword) {
    callback(new Error(rule.message));
  } else {
    callback();
  }
};

// Parameter validation rules
const rules = {
  email: [
    {required: true, message: "Please enter your email"},
    {validator: proxy.Valid.email, message: "Please enter a correct email"},
  ],
  password: [{required: true, message: "Please enter your password"}],
  emailCode: [{required: true, message: "Please enter your email verification code"}],
  nickName: [{required: true, message: "Please enter your nickname"}],
  newPassword: [
    {required: true, message: "Please enter your password"},
    {
      validator: proxy.Valid.password,
      message: "The password must contain numbers, letters and special characters and be at least 8 characters long",
    },
  ],
  rePassword: [
    {required: true, message: "Please enter your password"},
    {
      validator: checkRePassword,
      message: "The two passwords you entered do not match",
    },
  ],
  captchaCode: [{required: true, message: "Please enter the image verification code"}],
};

// Operation type: 0: Register; 1: Login; 2: Reset password
const opType = ref(1);

// Switch window
const switchPanel = (type) => {
  opType.value = type;

  resetForm();
};

const captchaUrl = proxy.globalInfo.captchaUrl;
// 发送验证码
const getCaptchaCheckUrl = ref(captchaUrl);
const getCaptchaEmailUrl = ref(captchaUrl);

// 刷新验证码
const refreshCaptcha = (type) => {
  if (type == 0) {
    getCaptchaCheckUrl.value =
        captchaUrl + "?type=" + type + "&time=" + new Date().getTime();
  } else {
    getCaptchaEmailUrl.value =
        captchaUrl + "?type=" + type + "&time=" + new Date().getTime();
  }
};

// 发送邮箱验证码弹窗相关
const smcFormData = ref({});
const smcFormDataRef = ref();

const smcDialogConfig = reactive({
  show: false,
  title: "Send email verification code",
  buttons: [
    {
      type: "primary",
      text: "Send",
      click: (e) => {
        sendEmailCode();
      },
    },
  ],
});

//Display the pop-up window for sending email verification code
const showSendEmailCodeDialog = () => {
  formDataRef.value.validateField("email", (valid) => {
    if (!valid) {
      return;
    }

    smcDialogConfig.show = true;

    // 清空表单数据重新赋值
    nextTick(() => {
      refreshCaptcha(1);
      smcFormDataRef.value.resetFields();
      smcFormData.value = {
        email: formData.value.email,
      };
    });
  });
};

// 发送邮箱验证码
const sendEmailCode = () => {
  smcFormDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    const params = Object.assign({}, smcFormData.value);
    params.type = opType.value == 0 ? 0 : 1;
    let result = await proxy.Request({
      url: api.sendEmailCode,
      params: params,
      errorCallback: () => {
        refreshCaptcha(1);
      },
    });

    if (!result) {
      return;
    }
    proxy.Message.success("The verification code was sent successfully, please log in to your email to check");
    // 发送成功关闭弹窗
    smcDialogConfig.show = false;
  });
};

// 重置表单
const resetForm = () => {
  formDataRef.value.resetFields();
  formData.value = {};

  // 登录时从cookie取用户名密码赋值到表单
  if (opType.value == 1) {
    let cookieLoginInfo = proxy.VueCookies.get("loginInfo");
    if (cookieLoginInfo) {
      formData.value = cookieLoginInfo;
    }
  }
};

// 提交表单
const doSubmit = () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = {};
    Object.assign(params, formData.value);

    // 注册或找回密码
    if (opType.value === 0 || opType.value === 2) {
      params.password = params.newPassword;
      delete params.newPassword;
      delete params.rePassword;
    }

    // 登录时从cookie中取密码
    if (opType.value === 1) {
      let cookieLoginInfo = proxy.VueCookies.get("loginInfo");
      let cookiePassword =
          cookieLoginInfo == null ? null : cookieLoginInfo.password;
      if (params.password !== cookiePassword) {
        params.password = md5(params.password);
      }
    }
    let url = null;
    if (opType.value === 0) {
      url = api.register;
    } else if (opType.value === 1) {
      url = api.login;
    } else if (opType.value === 2) {
      url = api.resetPwd;
    }

    let result = await proxy.Request({
      url: url,
      params: params,
      errorCallback: () => {
        refreshCaptcha(0);
      },
    });
    if (!result) {
      return;
    }

    // 注册
    if (opType.value == 0) {
      proxy.Message.success("注册成功，请登录");
      switchPanel(1);
    } else if (opType.value == 1) {
      // 登录
      // 如果选择记住密码，则将用户密码记录到cookie
      if (params.rememberMe) {
        const loginInfo = {
          email: params.email,
          password: params.password,
          rememberMe: params.rememberMe,
        };
        // 保存7天
        proxy.VueCookies.set("loginInfo", loginInfo, "7d");
      } else {
        proxy.VueCookies.remove("loginInfo");
      }
      proxy.Message.success("Login successful");
      // 临时保持用户信息，关闭浏览器时删除
      proxy.VueCookies.set("userInfo", result.data, 0);

      // 重定向到原页面
      if (route.query.redirectUrl == "/login") {
        route.query.redirectUrl = "/";
      }
      const redirectUrl = route.query.redirectUrl || "/";
      router.push(redirectUrl);
    } else if (opType.value == 2) {
      // 重置密码
      proxy.Message.success("重置密码成功，请登录");
      switchPanel(1);
    }
  });
};

// qq登录
let qqLogin = async () => {
  let result = proxy.Request({
    url: api.qqLogin,
    params: {
      redirectUrl: route.query.redirectUrl || "",
    },
  });
  if (!result) {
    return;
  }
  proxy.VueCookies.remove("userInfo");
  document.location.href = result.data;
};

onMounted(() => {
  switchPanel(1);
});
</script>
<style lang="scss" scoped>
.login-body {
  min-height: calc(100vh);
  background-size: cover;
  background-image: url("../assets/images/login_bg.jpg");
  display: flex;

  .bg {
    flex: 1;
    background-position: center;
    background-size: 500px;
    background-repeat: no-repeat;
    background-image: url("../assets/images/login_img.png");
  }

  .login-panel {
    width: 400px;
    margin-right: 15%;
    margin-top: calc((100vh - 500px) / 2);


    .login-register {
      padding: 25px;
      background: #fff;
      border-radius: 5px;

      .login-title {
        text-align: center;
        font-size: 18px;
        font-weight: bold;
        margin-bottom: 20px;
      }

      .send-email-panel {
        display: flex;
        width: 100%;
        justify-content: space-between;

        .send-email-btn {
          margin-left: 5px;
        }
      }

      .remeberme-panel {
        width: 100px;
      }

      .no-account {
        width: 100%;
        display: flex;
        justify-content: space-between;
      }

      .op-btn {
        width: 100%;
      }
    }
  }
  .el-form {
    border: 1px solid ; /* Set the border color to your desired value */
    border-color: #7e7e7e;
    border-radius: 5px; /* Set the border radius as needed */
  }
  .captcha-code-panel {
    width: 100%;
    display: flex;

    .captcha-code {
      margin-left: 5px;
      cursor: pointer;
    }
  }

  .login-btn-qq {
    margin-top: 20px;
    text-align: center;
    display: flex;
    align-items: center;
    justify-content: center;

    img {
      cursor: pointer;
      margin-left: 10px;
      width: 20px;
    }
  }
}
</style>
