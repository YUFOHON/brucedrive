<template>
  <div>登录中，请勿刷新页面</div>
</template>
<script setup>
import { ref, reactive, getCurrentInstance, nextTick } from "vue";
import { useRouter, useRoute } from "vue-router";

const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();

const api = {
  qqLoginCallback: "/qqLogin/callback",
};

// 进行QQ登录
const login = async () => {
  let result = await proxy.Request({
    url: api.qqLoginCallback,
    params: router.currentRoute.value.query,
    errorCallback: () => {
      router.push("/");
    },
  });

  if (!result) {
    return;
  }

  // 临时保持用户信息，关闭浏览器时删除
  proxy.VueCookies.set("userInfo", result.data.userInfo, 0);

  // 重定向到原页面
  let redirectUrl = result.data.redirectUrl || "/";
  if (redirectUrl == "/login") {
    redirectUrl = "/";
  }
  router.push(redirectUrl);
};
login();
</script>
