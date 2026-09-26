<script setup>
import { ref, onMounted } from 'vue';
import { queryAllApi, addApi, queryByIdApi, updateApi, deleteByIdApi } from '@/api/dept';
import { ElMessage, ElMessageBox } from 'element-plus';
import { hasPermission } from '@/utils/permission';

//钩子函数
onMounted(() => {
  search();
})

//部门列表数据
const deptList = ref([])
const search = async () => {
  const result = await queryAllApi();
  deptList.value = result.data;
}

//Dialog对话框
const dialogVisible = ref(false);
const formTitle = ref('');
const dept = ref({name: ''});
const deptFormRef = ref(null);

//重置表单
const resetForm = () => {
  deptFormRef.value.resetFields();
}

//新增部门
const addDept = () => {
  formTitle.value = '新增部门';
  dialogVisible.value = true;
  dept.value = {name: ''};
}

//修改部门
const editDept = async (row) => {
  formTitle.value = '修改部门';
  dialogVisible.value = true;
  dept.value = {name: ''};

  const result = await queryByIdApi(row.id);
  if(result.code){
    dept.value = result.data;
  }
}

//删除部门
const deleteDept = (row) => {
  ElMessageBox.confirm('此操作将永久删除该部门, 是否继续?','提示',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'}
  ).then(async () => { //确认
    const result = await deleteByIdApi(row.id);
    if(result.code){
      ElMessage.success('删除成功');
      search();
    }else{
      ElMessage.error(result.msg);
    }
  }).catch(() => {}) //取消
}

//保存部门
const save = async () => {
  await deptFormRef.value.validate(async (valid) => {
    if(!valid) return;

    let result = null;
    if(dept.value.id){ //修改
      result = await updateApi(dept.value);
    }else{ //新增
      result = await addApi(dept.value);
    }

    if(result.code){
      ElMessage.success('操作成功');
      dialogVisible.value = false;
      resetForm();
      search();
    }else{
      ElMessage.error(result.msg);
    }
  })
}

//表单校验规则
const rules = ref({
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
  ]
})
</script>

<template>
  <h1>部门管理</h1>
  <!-- 工具栏权限一个都没有时整块不渲染(含换行), 避免只读角色留一片空白 -->
  <template v-if="hasPermission(['department:create'])">
    <el-button type="primary" @click="addDept" style="float: right;" v-permission="'department:create'"> + 新增部门</el-button>
    <br>
    <br>
  </template>

  <!-- 表格 -->
  <el-table :data="deptList" border style="width: 100%">
    <el-table-column type="index" label="序号" width="100" align="center"/>
    <el-table-column prop="name" label="部门名称" width="300" align="center"/>
    <el-table-column prop="updateTime" label="最后修改时间" width="300" align="center"/>
    <!-- 一个操作权限都没有时整列隐藏, 避免留一列空白"操作" -->
    <el-table-column v-if="hasPermission(['department:update', 'department:delete'])" fixed="right" label="操作" align="center">
      <template #default="{row}">
        <el-button size="small" @click="editDept(row)" v-permission="'department:update'">修改</el-button>
        <el-button size="small" type="danger" @click="deleteDept(row)" v-permission="'department:delete'">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <!-- Dialog对话框 -->
  <el-dialog v-model="dialogVisible" :title="formTitle" width="30%" @close="resetForm">
    <el-form :model="dept" :rules="rules" ref="deptFormRef">
      <el-form-item label="部门名称" prop="name" label-width="80px">
        <el-input v-model="dept.name" autocomplete="off"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>
