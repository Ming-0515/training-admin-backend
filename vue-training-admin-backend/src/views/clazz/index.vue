<script setup>
import { ref, watch, onMounted } from 'vue';
import { queryPageApi, addApi, queryByIdApi, updateApi, deleteByIdApi } from '@/api/clazz';
import { queryAllApi as queryAllEmpApi } from '@/api/emp';
import { ElMessage, ElMessageBox } from 'element-plus';
import { hasPermission } from '@/utils/permission';

//元数据
//学科列表数据
const subjects = ref([{ name: 'Java', value: 1 }, { name: '前端', value: 2 }, { name: '大数据', value: 3 }, { name: 'Python', value: 4 }, { name: 'Go', value: 5 }, { name: '嵌入式', value: 6 }])
//班主任列表数据
const headTeachers = ref([])

//搜索表单对象
const searchClazz = ref({begin: '', end: '', date: [], name: ''})

//班级列表数据
const clazzList = ref([])

//分页
const pageInfo = ref({currentPage: 1, pageSize: 10, total: 0})

//侦听searchClazz的date属性
watch(() => searchClazz.value.date, (newVal, oldVal) => {
  // 注意 newVal 可能是 null(点日期框的清除按钮), 不能直接取 length
  if(newVal && newVal.length > 0){
    searchClazz.value.begin = newVal[0];
    searchClazz.value.end = newVal[1];
  }else {
    searchClazz.value.begin = '';
    searchClazz.value.end = '';
  }
})

//钩子函数
onMounted(() => {
  search(); //查询班级列表数据
  queryAllEmps(); //查询所有班主任列表数据
})

//查询所有班主任数据
const queryAllEmps = async () => {
  const result = await queryAllEmpApi();
  if(result.code){
    headTeachers.value = result.data;
  }
}

//查询班级列表
const search = async () => {
  const result = await queryPageApi(searchClazz.value.begin, searchClazz.value.end, searchClazz.value.name,
                                    pageInfo.value.currentPage, pageInfo.value.pageSize);
  if(result.code){
    clazzList.value = result.data.rows;
    pageInfo.value.total = result.data.total;
  }
}

//清空
const clear = () => {
  searchClazz.value = {begin: '', end: '', date: [], name: ''};
  search();
}

//每页展示记录数变化
const handleSizeChange = (val) => {
  pageInfo.value.pageSize = val;
  search();
}

//页码变化时触发
const handleCurrentChange = (val) => {
  pageInfo.value.currentPage = val;
  search();
}

//Dialog对话框
const dialogFormVisible = ref(false);
const labelWidth = ref(80);
const dialogTitle = ref('');
const clazz = ref({id: '', name: '', room: '', beginDate: '', endDate: '', subject: '', headTeacherId: ''});
//表单引用
const clazzFormRef = ref();

//重置表单对象
const resetForm = () => {
  clazz.value = {id: '', name: '', room: '', beginDate: '', endDate: '', subject: '', headTeacherId: ''};
}

//重置表单的校验规则-提示信息
const resetFields = (formEl) => {
  if(formEl){
    formEl.resetFields();
  }
}

//新增班级
const addClazz = () => {
  dialogFormVisible.value = true;
  dialogTitle.value = '新增班级';
  resetForm();
}

//修改班级
const edit = async (id) => {
  resetForm();
  dialogTitle.value = '修改班级';
  dialogFormVisible.value = true;
  resetFields(clazzFormRef.value);

  const result = await queryByIdApi(id);
  if(result.code){
    clazz.value = result.data;
  }
}

//保存班级
const save = () => {
  //表单校验
  if(!clazzFormRef.value) return;
  clazzFormRef.value.validate(async (valid) => { //valid 表示是否校验通过: true 通过 / false 不通过
    if(valid){ //通过
      let result;
      if(clazz.value.id){ //修改
        result = await updateApi(clazz.value);
      }else { //新增
        result = await addApi(clazz.value);
      }

      if(result.code){ //成功
        ElMessage.success('操作成功');
        dialogFormVisible.value = false;
        search();
      }else { //失败
        ElMessage.error(result.msg);
      }
    }else { //不通过
      return false;
    }
  })
}

//表单校验规则
const rules = ref({
  name: [
    { required: true, message: '班级名称为必填项', trigger: 'blur' },
    { min: 4, max: 30, message: '班级名称长度为4-30个字', trigger: 'blur' }
  ],
  room: [
    { min: 1, max: 20, message: '班级教室长度为1-20个字', trigger: 'blur' }
  ],
  beginDate: [
    { required: true, message: '开课时间为必填项', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '结课时间为必填项', trigger: 'change' }
  ],
  // 班主任可为空(后端允许 headTeacherId 不传), 故不做必填校验
  subject: [
    { required: true, message: '学科为必填项', trigger: 'change' }
  ]
})

//取消
const closeDialog = () => {
  dialogFormVisible.value = false;
  resetFields(clazzFormRef.value);
}

//删除班级
const delById = (id) => {
  //弹出确认框
  ElMessageBox.confirm('您确认删除此数据吗?', '删除班级',
    { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => { //确认
    const result = await deleteByIdApi(id);
    if(result.code){
      ElMessage.success('删除成功');
      search();
    }else{
      ElMessage.error(result.msg);
    }
  }).catch(() => { //取消
    ElMessage.info('取消删除');
  })
}
</script>

<template>
  <div>
    <div id="title">班级管理</div>
    <br>
  </div>

  <!-- 搜索栏 -->
  <el-form :inline="true" :model="searchClazz" class="demo-form-inline">
    <el-form-item label="班级名称">
      <el-input v-model="searchClazz.name" placeholder="请输入班级名称" />
    </el-form-item>

    <el-form-item label="结课时间">
      <el-date-picker
        v-model="searchClazz.date"
        type="daterange"
        range-separator=" 至 "
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DD"
      />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="info" @click="clear">清空</el-button>
    </el-form-item>
  </el-form>

  <!-- 功能按钮: 工具栏权限一个都没有时整块不渲染(含换行), 避免只读角色留一片空白 -->
  <template v-if="hasPermission(['class:create'])">
    <el-button v-permission="'class:create'" type="success" @click="addClazz">+ 新增班级</el-button>
    <br>
    <br>
  </template>

  <!-- 数据展示表格 -->
  <el-table :data="clazzList" border style="width: 100%" fit>
    <el-table-column type="index" label="序号" width="55" align="center"/>
    <el-table-column prop="name" label="班级名称" align="center" width="200px"/>
    <el-table-column prop="room" label="班级教室" align="center" width="100px"/>
    <el-table-column prop="headTeacherName" label="班主任" align="center" width="100px"/>
    <el-table-column prop="beginDate" label="开课时间" align="center" width="150px"/>
    <el-table-column prop="endDate" label="结课时间" align="center" width="150px"/>
    <el-table-column prop="subject" label="学科" align="center" width="100px">
      <template #default="scope">
        <span v-if="scope.row.subject == 1">Java</span>
        <span v-else-if="scope.row.subject == 2">前端</span>
        <span v-else-if="scope.row.subject == 3">大数据</span>
        <span v-else-if="scope.row.subject == 4">Python</span>
        <span v-else-if="scope.row.subject == 5">Go</span>
        <span v-else-if="scope.row.subject == 6">嵌入式</span>
      </template>
    </el-table-column>
    <el-table-column prop="status" label="状态" align="center" width="130px"/>
    <el-table-column prop="updateTime" label="最后修改时间" align="center"/>
    <!-- 一个操作权限都没有时整列隐藏, 避免留一列空白"操作" -->
    <el-table-column v-if="hasPermission(['class:update', 'class:delete'])" label="操作" align="center">
      <template #default="scope">
        <el-button v-permission="'class:update'" type="primary" size="small" @click="edit(scope.row.id)">编辑</el-button>
        <el-button v-permission="'class:delete'" type="danger" size="small" @click="delById(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <br>

  <!-- 分页条 -->
  <el-pagination
    v-model:current-page="pageInfo.currentPage"
    v-model:page-size="pageInfo.pageSize"
    :page-sizes="[5, 10, 20, 50, 100]"
    layout="total, sizes, prev, pager, next, jumper"
    :total="pageInfo.total"
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
  />

  <!-- 新增班级/修改班级的对话框 -->
  <el-dialog v-model="dialogFormVisible" :title="dialogTitle" width="35%">
    <el-form :model="clazz" ref="clazzFormRef" :rules="rules">
      <el-form-item label="班级名称" :label-width="labelWidth" prop="name">
        <el-input v-model="clazz.name" placeholder="请输入班级名称" />
      </el-form-item>

      <el-form-item label="班级教室" :label-width="labelWidth" prop="room">
        <el-input v-model="clazz.room" placeholder="请输入班级教室" />
      </el-form-item>

      <el-form-item label="开课时间" :label-width="labelWidth" prop="beginDate">
        <el-date-picker v-model="clazz.beginDate" type="date" placeholder="请选择开课时间" value-format="YYYY-MM-DD" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="结课时间" :label-width="labelWidth" prop="endDate">
        <el-date-picker v-model="clazz.endDate" type="date" placeholder="请选择结课时间" value-format="YYYY-MM-DD" style="width: 100%;" />
      </el-form-item>

      <el-form-item label="班主任" :label-width="labelWidth" prop="headTeacherId">
        <el-select v-model="clazz.headTeacherId" placeholder="请选择班主任" style="width: 100%;">
          <el-option v-for="emp in headTeachers" :key="emp.id" :label="emp.name" :value="emp.id"></el-option>
        </el-select>
      </el-form-item>

      <el-form-item label="学科" :label-width="labelWidth" prop="subject">
        <el-select v-model="clazz.subject" placeholder="请选择学科" style="width: 100%;">
          <el-option v-for="s in subjects" :key="s.value" :label="s.name" :value="s.value"></el-option>
        </el-select>
      </el-form-item>
    </el-form>

    <!-- 底部按钮 -->
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.container {
  margin: 15px 0px;
}

#title {
  font-size: 20px;
  font-weight: 600;
}
</style>
