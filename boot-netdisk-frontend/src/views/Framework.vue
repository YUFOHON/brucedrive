<template>
  <div class="framework">
    <div class="header">
      <div class="logo">
        <span class="iconfont "></span>

        <div class="name">Bruce Drive</div>
      </div>
      <!-- File upload area -->
      <div class="right-panel">
        <el-popover
            :width="800"
            trigger="click"
            v-model:visible="showUploader"
            :offset="20"
            transition="none"
            :hide-after="0"
            :popper-style="{ padding: '0px' }"
        >
          <template #reference>
            <span class="iconfont icon-transfer"></span>
          </template>
          <template #default>
            <Uploader
                ref="uploaderRef"
                @uploadCallback="uploadCallback"
            ></Uploader>
          </template>
        </el-popover>
        <!-- drop down -->
        <el-dropdown>
          <div class="user-info">
            <div class="avatar">
              <Avatar
                  :userId="userInfo.userId"
                  :avatar="userInfo.avatar"
                  :timestamp="timestamp"
                  :width="46"
              ></Avatar>
            </div>
            <span class="nick-name">{{ userInfo.nickName }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="updateAvatar"
              >Change Avatar
              </el-dropdown-item
              >
              <el-dropdown-item @click="updatePassword"
              >Change Password
              </el-dropdown-item
              >
              <el-dropdown-item @click="logout">Log out</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    <div class="body">
      <!-- 左侧菜单区域 -->
      <div class="left-sider">
        <div class="menu-list">
          <div
              @click="switchMenu(item)"
              :class="[
              'menu-item',
              item.menuCode == currentMenu.menuCode ? 'active' : '',
            ]"
              v-for="item in menus"
          >
            <!-- only admin visible -->
            <template v-if="item.allShow || userInfo.isAdmin">
              <div :class="['iconfont', 'icon-' + item.icon]"></div>
              <div class="text">{{ item.name }}</div>
            </template>
          </div>
        </div>
        <div class="menu-sub-list">
          <div
              @click="switchMenu(sub)"
              :class="['menu-item-sub', sub.path == currentPath ? 'active' : '']"
              v-for="sub in currentMenu.children"
          >
<span
    :class="['iconfont', 'icon-' + sub.icon]"
    v-if="sub.icon"
></span>
            <span class="text">{{ sub.name }}</span>
          </div>

          <div class="tips" v-if="currentMenu && currentMenu.tips">
            {{ currentMenu.tips }}
          </div>
          <div class="space-info">
            <div>Space usage</div>
            <div class="percent">
              <el-progress
                  :percentage="
Math.floor(
(useSpaceInfo.usedSpace / useSpaceInfo.totalSpace) * 10000
) / 100
"
                  color="#409ef"
              ></el-progress>
            </div>
            <div class="space-use">
              <div class="use">
                {{ proxy.Utils.size2Str(useSpaceInfo.usedSpace) }}/{{
                  proxy.Utils.size2Str(useSpaceInfo.totalSpace)
                }}
              </div>
              <div class="iconfont icon-refresh" @click="getUserSpace"></div>
            </div>
          </div>
        </div>
      </div>
      <div class="body-content">
        <router-view v-slot="{ Component }">
          <component
              ref="routerViewRef"
              :is="Component"
              @uploadFile="uploadFile"
              @reloadUserSpace="getUserSpace"
          ></component>
        </router-view>
      </div>
    </div>

    <!-- 修改头像 -->
    <UpdateAvatar
        ref="UpdateAvatarRef"
        @updateAvatar="reloadAvatar"
    ></UpdateAvatar>

    <!-- 修改密码 -->
    <UpdatePassword ref="UpdatePasswordRef"></UpdatePassword>
  </div>
</template>
<script setup>
import {ref, getCurrentInstance, watch, nextTick} from "vue";
import {useRouter, useRoute} from "vue-router";
import UpdateAvatar from "./UpdateAvatar.vue";
import UpdatePassword from "./UpdatePassword.vue";
import Uploader from "./main/Uploader.vue";

const {proxy} = getCurrentInstance();
const router = useRouter();
const route = useRoute();

const api = {
  logout: "/logout",
  getUserSpace: "/getUserSpace",
};

const userInfo = ref(proxy.VueCookies.get("userInfo"));
console.log(userInfo.value);
if (userInfo.value == null) {
  router.push(
      "/login?redirectUrl=" + encodeURI(router.currentRoute.value.path)
  );
}

// Menu list
const menus = [
  {
    icon: "cloude",
    name: "Home",
    path: "/main/all",
    menuCode: "main",
    allShow: true,
    children: [
      {
        icon: "all",
        name: "All",
        category: "all",
        path: "/main/all",
      },
      {
        icon: "video",
        name: "Video",
        category: "video",
        path: "/main/video",
      },
      {
        icon: "music",
        name: "Audio",
        category: "audio",
        path: "/main/audio",
      },
      {
        icon: "image",
        name: "Picture",
        category: "image",
        path: "/main/image",
      },
      {
        icon: "doc",
        name: "Document",
        category: "doc",
        path: "/main/doc",
      },
      {
        icon: "more",
        name: "Other",
        category: "others",
        path: "/main/others",
      },
    ],
  },
  {
    icon: "share",
    name: "Share",
    path: "/share",
    menuCode: "share",
    allShow: true,
    children: [
      {
        name: "Share Record",
        path: "/share",
      },
    ],
  },
  {
    icon: "del",
    name: "Recycle Bin",
    path: "/recycle",
    menuCode: "recycle",
    allShow: true,
    tips: "Recycle Bin saves files deleted within 10 days",
    children: [
      {
        name: "Deleted Files",
        path: "/recycle",
      },
    ],
  },
  {
    icon: "settings",
    name: "Settings",
    path: "/settings/fileList",
    menuCode: "settings",
    allShow: false,
    children: [
      {
        name: "User Files",
        path: "/settings/fileList",
      },
      {
        name: "User Management",
        path: "/settings/userList",
      },
      {
        name: "System Settings",
        path: "/settings/systemSettings",
      },
    ],
  },
];
// 当前选中菜单
const currentMenu = ref({});
const currentPath = ref();

// 切换菜单
const switchMenu = (data) => {
  if (!data.path || data.menuCode == currentMenu.value.menuCode) {
    return;
  }

  router.push(data.path);
};

// 修改头像
const timestamp = ref(0);
const UpdateAvatarRef = ref();

// 显示修改头像弹窗
const updateAvatar = () => {
  UpdateAvatarRef.value.show(userInfo.value);
};

// 修改头像后触发，刷新图片
const reloadAvatar = () => {
  userInfo.value = proxy.VueCookies.get("userInfo");
  timestamp.value = new Date().getTime();
};

// 修改密码
const UpdatePasswordRef = ref();
const updatePassword = () => {
  UpdatePasswordRef.value.show(userInfo.value);
};

// 退出登录
const logout = async () => {
  proxy.Confirm("Are you sure you want to log out？", async () => {
    let result = await proxy.Request({
      url: api.logout,
    });

    if (!result) {
      return;
    }
    proxy.VueCookies.remove("userInfo");
    router.push("/login");
  });
};

const uploaderRef = ref();
const showUploader = ref(false);
// 上传文件
const uploadFile = (data) => {
  const {file, filePid} = data;

  showUploader.value = true;
  uploaderRef.value.addFile(file, filePid);
};

const routerViewRef = ref();
// 上传文件回调
const uploadCallback = () => {
  nextTick(() => {
    // 重新加载文件列表
    routerViewRef.value.reload();
    // 更新用户空间
    getUserSpace();
  });
};

const useSpaceInfo = ref({usedSpace: 0, totalSpace: 1});
// 获取用户空间信息
const getUserSpace = async () => {
  let result = await proxy.Request({
    url: api.getUserSpace,
    showLoading: false,
  });

  if (!result) {
    return;
  }

  useSpaceInfo.value = result.data;
};
getUserSpace();

watch(
    () => route,
    (newVal, oldVal) => {
      // 监听菜单切换
      if (newVal.meta.menuCode) {
        const menu = menus.find((item) => {
          return item.menuCode === newVal.meta.menuCode;
        });
        currentMenu.value = menu;
        currentPath.value = newVal.path;
      }
    },
    {immediate: true, deep: true}
);
</script>
<style lang="scss" scoped>
.header {
  box-shadow: 0 3px 10px 0 rgb(0 0 0 / 6%);
  height: 56px;
  padding-left: 24px;
  padding-right: 24px;
  position: relative;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .logo {
    display: flex;
    align-items: center;

    .icon-pan {
      font-size: 40px;
      color: #800080;
    }

    .name {
      font-weight: bold;
      margin-left: 5px;
      font-size: 25px;
      color: #800080;
    }
  }

  .right-panel {
    display: flex;
    align-items: center;

    .icon-transfer {
      cursor: pointer;
    }

    .user-info {
      margin-right: 10px;
      display: flex;
      align-items: center;
      cursor: pointer;

      .avatar {
        margin: 0 5px 0 15px;
      }

      .nick-name {
        color: #05a1f5;
      }
    }
  }
}

.body {
  display: flex;

  .left-sider {
    border-right: 1px solid #f1f2f4;
    display: flex;

    .menu-list {
      height: calc(100vh - 56px);
      width: 80px;
      box-shadow: 0 3px 10px 0 rgb(0 0 0 / 6%);
      border-right: 1px solid #f1f2f4;

      .menu-item {
        text-align: center;
        font-size: 14px;
        font-weight: bold;
        padding: 20px 0;
        cursor: pointer;

        &:hover {
          background: #f3f3f3;
        }

        .iconfont {
          font-weight: normal;
          font-size: 28px;
        }
      }

      .active {
        .iconfont {
          color: #06a7ff;
        }

        .text {
          color: #06a7ff;
        }
      }
    }

    .menu-sub-list {
      width: 200px;
      padding: 20px 10px 0;
      position: relative;

      .menu-item-sub {
        text-align: center;
        line-height: 40px;
        border-radius: 5px;
        cursor: pointer;

        &:hover {
          background: #f3f3f3;
        }

        .iconfont {
          font-size: 14px;
          margin-right: 20px;
        }

        .text {
          font-size: 13px;
        }
      }

      .active {
        background: #eef9fe;

        .iconfont {
          color: #800080;
        }

        .text {
          color: #800080;
        }
      }

      .tips {
        margin-top: 10px;
        color: #888;
        font-size: 13px;
      }

      .space-info {
        position: absolute;
        bottom: 10px;
        width: 100%;
        padding: 0 5px;

        .percent {
          padding-right: 10px;
        }

        .space-use {
          margin-top: 5px;
          color: #7e7e7e;
          display: flex;
          justify-content: space-around;

          .use {
            flex: 1;
          }

          .iconfont {
            cursor: pointer;
            margin-right: 20px;
            color: #800080;
          }
        }
      }
    }
  }

  .body-content {
    flex: 1;
    width: 0;
    padding-left: 20px;
  }
}
</style>
