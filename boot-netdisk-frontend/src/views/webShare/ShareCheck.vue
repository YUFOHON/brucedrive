<template>
  <div>
    <div class="share">
      <div class="body-content">
        <div class="logo">
          <span class="iconfont "></span>
          <span class="name">Bruce drive</span>
        </div>
        <div class="code-panel">
          <div class="file-info">
            <div class="avatar">
              <Avatar
                  :userId="shareInfo.userId"
                  :avatar="shareInfo.avatar"
                  :width="50"
              ></Avatar>
            </div>
            <div class="share-info">
              <div class="user-info">
                <span class="nick-name">{{ shareInfo.nickName }}</span>
                <span class="share-time"
                >Shared at {{ shareInfo.createTime }}</span
                >
              </div>
              <div class="file-name">Shared file: {{ shareInfo.fileName }}</div>
            </div>
          </div>
          <div class="code-body">
            <div class="tips">Please enter the extraction code: </div>
            <div class="input-area">
              <el-form
                  :model="formData"
                  :rules="rules"
                  ref="formDataRef"
                  @submit.prevent
              >
                <el-form-item prop="code">
                  <el-input
                      class="input"
                      clearable
                      v-model.trim="formData.code"
                      @keyup.enter="checkShare"
                  >
                  </el-input>
                  <el-button type="primary" @click="checkShare"
                  >Extract file</el-button
                  >
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>



<script setup>
import { ref, getCurrentInstance } from "vue";
import { useRouter, useRoute } from "vue-router";

const { proxy } = getCurrentInstance();
const router = useRouter();
const route = useRoute();

const api = {
  getShareInfo: "/webShare/getShareFile",
  checkShareCode: "/webShare/checkShareCode",
};

const formData = ref({});
const formDataRef = ref();
const rules = {
  code: [
    { required: true, message: "Please enter the extraction code" },
    { min: 5, message: "The extraction code is 5 digits" },
    { max: 5, message: "The extraction code is 5 digits" },
  ],
};

const shareInfo = ref({});
const shareId = route.params.shareId;

const getShareInfo = async () => {
  let result = await proxy.Request({
    url: api.getShareInfo,
    params: {
      shareId: shareId,
    },
  });

  if (!result) {
    return;
  }

  shareInfo.value = result.data;
};

getShareInfo();

// 校验提取码
const checkShare = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    let result = await proxy.Request({
      url: api.checkShareCode,
      params: {
        shareId: shareId,
        code: formData.value.code,
      },
    });

    if (!result) {
      return;
    }

    router.push(`/share/${shareId}`);
  });
};
</script>
<style lang="scss" scoped>
.share {
  height: calc(100vh);
  background: url("@/assets/images/share_bg.png");
  background-repeat: repeat;
  background-position: 0 bottom;
  background-color: #eef2f6;
  display: flex;
  justify-content: center;

  .body-content {
    margin-top: calc(100vh / 5);
    width: 500px;

    .logo {
      display: flex;
      align-items: center;
      justify-content: center;

      .icon-pan {
        font-size: 60px;
        color: #409eff;
      }

      .name {
        font-weight: bold;
        margin-left: 5px;
        font-size: 25px;
        color: #409eff;
      }
    }

    .code-panel {
      margin-top: 20px;
      background: #fff;
      border-radius: 5px;
      overflow: hidden;
      box-shadow: 0 0 7px 1px #5757574f;

      .file-info {
        padding: 10px 20px;
        background: #409eff;
        color: #fff;
        display: flex;
        align-items: center;

        .avatar {
          margin-right: 5px;
        }

        .share-info {
          .user-info {
            display: flex;
            align-items: center;

            .nick-name {
              font-size: 15px;
            }

            .share-time {
              margin-left: 20px;
              font-size: 12px;
            }
          }

          .file-name {
            margin-top: 10px;
            font-size: 12px;
          }
        }
      }

      .code-body {
        padding: 30px 20px 60px 20px;

        .tips {
          font-weight: bold;
        }

        .input-area {
          margin-top: 10px;

          .input {
            flex: 1;
            margin-right: 10px;
          }
        }
      }
    }
  }
}
</style>
