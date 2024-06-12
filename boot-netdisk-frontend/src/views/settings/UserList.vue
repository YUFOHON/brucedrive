<template>
  <div>
    <div class="top-panel">
      <el-form
          :model="searchFormData"
          ref="searchFormDataRef"
          label-width="80px"
          @submit.prevent
      >
        <el-row>
          <el-col :span="6">
            <el-form-item label="Nickname">
              <el-input
                  clearable
                  placeholder="Support fuzzy search"
                  v-model="searchFormData.nickNameFuzzy"
                  @keyup.enter="loadDataList"
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="Status">
              <el-select
                  clearable
                  placeholder="Please select status"
                  v-model="searchFormData.status"
              >
                <el-option :value="1" label="Enable"></el-option>
                <el-option :value="0" label="Disable"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4" :style="{ 'padding-left': '10px' }">
            <el-button type="primary" @click="loadDataList">Query</el-button>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- User List -->
    <div>
      <Table
          ref="dataTableRef"
          :columns="columns"
          :dataSource="tableData"
          :fetch="loadDataList"
          :initFetch="true"
          :options="tableOptions"
      >
        <template #avatar="{ index, row }">
          <div class="avatar">
            <Avatar :userId="row.userId" :avatar="row.qqAvatar"></Avatar>
          </div>
        </template>
        <template #space="{ index, row }">
          {{ proxy.Utils.size2Str(row.usedSpace) }}/{{
            proxy.Utils.size2Str(row.totalSpace)
          }}
        </template>
        <template #status="{ index, row }">
          <span v-if="row.status == 1" style="color: #529b2e">Enabled</span>
          <span v-if="row.status == 0" style="color: #f56c62">Disabled</span>
        </template>
        <template #op="{ index, row }">
          <span class="a-link" @click="changeUserSpace(row)">Allocate</span>
          <el-divider direction="vertical"></el-divider>
          <span class="a-link" @click="updateUserStatus(row)">{{
              row.status == 0 ? "Enabled" : "Disable"
            }}</span>
        </template>
      </Table>
    </div>
    <Dialog
        :show="dialogConfig.show"
        :title="dialogConfig.title"
        :buttons="dialogConfig.buttons"
        :showCancel="false"
        width="400px"
        @close="dialogConfig.show = false"
    >
      <el-form
          :model="dialogFormData"
          :rules="dialogRules"
          ref="dialogFormDataRef"
          label-width="80px"
          @submit.prevent
      >
        <el-form-item label="nickname">{{ dialogFormData.nickName }}</el-form-item>
        <el-form-item label="space size" prop="changeSpace">
          <el-input
              clearable
              placeholder="Please enter the space size"
              v-model.trim="dialogFormData.changeSpace"
              type="number"
          >
            <template #suffix>MB</template>
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
  loadDataList: "/admin/loadUserList",
  updateUserStatus: "/admin/updateUserStatus",
  changeUserSpace: "/admin/changeUserSpace",
};

const columns = [
  {
    label: "avatar",
    prop: "avatar",
    scopedSlots: "avatar",
    width: 80,
  },
  {
    label: "nickname",
    prop: "nickName",
  },
  {
    label: "email",
    prop: "email",
  },
  {
    label: "space usage",
    prop: "space",
    scopedSlots: "space",
  },
  {
    label: "join time",
    prop: "createTime",
  },
  {
    label: "last login time",
    prop: "lastLoginTime",
  },
  {
    label: "status",
    prop: "status",
    scopedSlots: "status",
    width: 80,
  },
  {
    label: "operation",
    prop: "op",
    width: 150,
    scopedSlots: "op",
  },
];
const dataTableRef = ref({});
const tableData = ref({});
const tableOptions = ref({
  exHeight: 20,
});

const searchFormData = ref({});
const searchFormDataRef = ref();

const dialogConfig = ref({
  show: false,
  title: "修改空间大小",
  buttons: [
    {
      type: "primary",
      text: "确定",
      click: (e) => {
        submitForm();
      },
    },
  ],
});

const dialogFormData = ref({});
const dialogFormDataRef = ref();
const dialogRules = {
  changeSpace: [
    { required: true, message: "请输入空间大小" },
    { validator: proxy.Valid.number, message: "空间大小只能是数字" },
  ],
};

// 加载用户列表
const loadDataList = async () => {
  let params = {
    pageNo: tableData.value.pageNo,
    pageSize: tableData.value.pageSize,
  };

  Object.assign(params, searchFormData.value);

  let result = await proxy.Request({
    url: api.loadDataList,
    params: params,
  });

  if (!result) {
    return;
  }

  tableData.value = result.data;
};

// 修改用户状态
const updateUserStatus = (row) => {
  proxy.Confirm(
    `你确定要【${row.status == 0 ? "启用" : "禁用"}】吗？`,
    async () => {
      let result = await proxy.Request({
        url: api.updateUserStatus,
        params: {
          userId: row.userId,
          status: row.status == 0 ? 1 : 0,
        },
      });

      if (!result) {
        return;
      }
      loadDataList();
    }
  );
};

// 修改用户空间
const changeUserSpace = (data) => {
  dialogConfig.value.show = true;
  nextTick(() => {
    dialogFormDataRef.value.resetFields();
    dialogFormData.value = Object.assign({}, data);
  });
};

// 提交修改
const submitForm = () => {
  dialogFormDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }

    let params = {};
    Object.assign(params, dialogFormData.value);
    let result = await proxy.Request({
      url: api.changeUserSpace,
      params: params,
    });

    if (!result) {
      return;
    }

    dialogConfig.value.show = false;
    proxy.Message.success("修改成功");
    loadDataList();
  });
};
</script>
<style lang="scss" scoped>
.top-panel {
  margin-top: 20px;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 25px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
  }
}
</style>
