<script setup>
import { ref, onMounted } from 'vue';
import { queryPageApi } from '@/api/log';

//钩子函数
onMounted(() => {
  search();
})

//日志列表数据
const logList = ref([])

//分页数据
const pageInfo = ref({currentPage: 1, pageSize: 15, total: 0})

//每页展示记录数变化
const handleSizeChange = (val) => {
  pageInfo.value.pageSize = val;
  search();
}
//页码变化
const handleCurrentChange = (val) => {
  pageInfo.value.currentPage = val;
  search();
}

//查询操作日志
const search = async () => {
  const result = await queryPageApi(pageInfo.value.currentPage, pageInfo.value.pageSize);
  if(result.code){
    logList.value = result.data.rows;
    pageInfo.value.total = result.data.total;
  }
}
</script>

<template>
  <div id="title">日志管理</div>
  <br>

  <!-- 数据展示表格 -->
  <el-table :data="logList" border style="width: 100%" fit size="small">
    <el-table-column prop="operatorName" label="操作人" align="center" width="80px"/>
    <el-table-column prop="operationTime" label="操作时间" align="center" width="150px"/>
    <el-table-column prop="className" label="类名" align="center" width="300px"/>
    <el-table-column prop="methodName" label="方法名" align="center" width="100px"/>
    <el-table-column prop="costTime" label="操作耗时(ms)" align="center" width="100px"/>
    <el-table-column prop="methodParams" label="请求参数" align="center" width="280px">
      <template #default="scope">
        <!-- 参数可能很长, 截断显示, 鼠标悬浮看完整内容 -->
        <el-popover effect="light" trigger="hover" placement="top" width="auto" popper-style="font-size:12px">
          <template #reference>
            <el-tag v-if="scope.row.methodParams && scope.row.methodParams.length <= 30">{{ scope.row.methodParams }}</el-tag>
            <el-tag v-else-if="scope.row.methodParams">{{ scope.row.methodParams.substring(0, 30) + "..." }}</el-tag>
          </template>
          <div>参数: {{ scope.row.methodParams }}</div>
        </el-popover>
      </template>
    </el-table-column>
    <el-table-column prop="returnValue" label="操作结果" align="center"/>
  </el-table>
  <br>

  <!-- 分页条 -->
  <el-pagination
    v-model:current-page="pageInfo.currentPage"
    v-model:page-size="pageInfo.pageSize"
    :page-sizes="[10, 20, 50, 100]"
    layout="total, sizes, prev, pager, next, jumper"
    :total="pageInfo.total"
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
  />
</template>

<style scoped>
#title {
  font-size: 20px;
  font-weight: 600;
}
</style>
