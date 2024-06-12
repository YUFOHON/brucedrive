<!-- Table Component -->
<template>
  <div>
    <el-table
        ref="dataTable"
        :data="dataSource.list || []"
        :height="tableHeight"
        :stripe="options.stripe"
        :border="options.border"
        header-row-class-name="table-header-row"
        highlight-current-row
        @row-click="handleRowClick"
        @selection-change="handleSelectChange"
    >
      <!-- selection selection box -->
      <el-table-column
          v-if="options.selectType && options.selectType == 'checkbox'"
          type="selection"
          width="50"
          align="center"
      ></el-table-column>

      <!-- Sequence number -->
      <el-table-column
          v-if="options.showIndex"
          label="Sequence number"
          type="index"
          width="60"
          align="center"
      ></el-table-column>

      <!-- Data columns -->
      <template v-for="(column, index) in columns">
        <template v-if="column.scopedSlots">
          <el-table-column
              :key="index"
              :prop="column.prop"
              :label="column.label"
              :align="column.align || 'left'"
              :width="column.width"
              :min-width="column.minWidth"
          >
            <template #default="scope">
              <slot
                  :name="column.scopedSlots"
                  :index="scope.$index"
                  :row="scope.row"
              ></slot>
            </template>
          </el-table-column>
        </template>
        <template v-else>
          <el-table-column
              :key="index"
              :prop="column.prop"
              :label="column.label"
              :align="column.align || 'left'"
              :width="column.width"
              :min-width="column.minWidth"
              :fixed="column.fixed"
          ></el-table-column>
        </template>
      </template>
    </el-table>

    <!-- Pagination -->
    <div class="pagination" v-if="showPagination">
      <el-pagination
          v-if="dataSource.totalCount"
          background
          :total="dataSource.totalCount"
          :page-sizes="[15, 30, 50, 100]"
          :page-size="dataSource.pageSize"
          :current-page.sync="dataSource.pageNo"
          :layout="layout"
          @size-change="handlePageSizeChange"
          @current-change="handlePageNoChange"
          style="text-align: right"
      ></el-pagination>
    </div>
  </div></template>
<script setup>
import { computed, ref } from "vue";

const emit = defineEmits(["rowSelected", "rowClick"]);
const props = defineProps({
  dataSource: Object, // 数据源
  showPagination: {
    //显示分页
    type: Boolean,
    default: true,
  },
  showPageSizes: {
    //显示分页选项
    type: Boolean,
    default: true,
  },
  options: {
    type: Object,
    default: {
      exHeight: 0,
      showIndex: false,
    },
  },
  columns: Array, // 列名称
  fetch: Function, // 获取数据的函数
  initFetch: {
    // 初始化时是否获取数据
    type: Boolean,
    default: true,
  },
});

// 分页样式
const layout = computed(() => {
  return `total, ${props.showPageSizes ? "sizes" : ""}, prev,pager,next,jumper`;
});

// 计算表格高度
// 顶部60、内容区域距离顶部20、内容上下文内间距15*2、分页区域高度46
const topHeight = 60 + 20 + 30 + 46;
const tableHeight = ref(
  props.options.tableHeight
    ? props.options.tableHeight
    : window.innerHeight - topHeight - props.options.exHeight
);

// 初始化
const init = () => {
  // 初始化时获取数据
  if (props.initFetch && props.fetch) {
    props.fetch();
  }
};
init();

const dataTable = ref();

// 清除选中
const clearSelection = () => {
  dataTable.value.clearSelection();
};

// 设置行选中
const setCurrentRow = (rowKey, rowValue) => {
  let row = props.dataSource.list.find((item) => {
    return item[rowKey] === rowValue;
  });
  dataTable.value.setCurrentRow(row);
};

// 暴露组件
defineExpose({ setCurrentRow, clearSelection });

// 行点击
const handleRowClick = (row) => {
  emit("rowClick", row);
};

// 多选
const handleSelectChange = (row) => {
  emit("rowSelected", row);
};

// 切换分页大小
const handlePageSizeChange = (size) => {
  props.dataSource.pageSize = size;
  props.dataSource.pageNo = 1;
  props.fetch();
};

// 切换页码
const handlePageNoChange = (pageNo) => {
  props.dataSource.pageNo = pageNo;
  props.fetch();
};
</script>
<style lang="scss" scoped>
.pagination {
  padding-top: 10px;
  padding-right: 10px;
}
.el-pagination {
  justify-content: right;
}

:deep(.el-table__cell) {
  padding: 4px 0;
}
</style>
