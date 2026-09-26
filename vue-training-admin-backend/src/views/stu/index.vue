<script setup>
import { ref, onMounted } from 'vue';
import { queryPageApi, addApi, queryByIdApi, updateApi, deleteByIdApi, violationApi } from '@/api/stu';
import { queryAllApi } from '@/api/clazz';
import { ElMessage, ElMessageBox } from 'element-plus';
import { hasPermission } from '@/utils/permission';

//下拉框数据 - 最高学历
const degreeOptions = ref([
  { name: '初中', value: 1 },
  { name: '高中', value: 2 },
  { name: '大专', value: 3 },
  { name: '本科', value: 4 },
  { name: '硕士', value: 5 },
  { name: '博士', value: 6 }
]);

//下拉框数据 - 性别
const genderOptions = ref([
  { name: '男', value: 1 },
  { name: '女', value: 2 }
]);

//下拉框数据 - 是否院校
const isCollegeOptions = ref([
  { name: '是', value: 1 },
  { name: '否', value: 0 }
]);

//钩子函数
onMounted(() => {
  search();
  queryAllClass();
})

//搜索表单数据
const searchForm = ref({ classId: '', degree: '', name: '' });

//表格数据
const stuList = ref([]);

//班级列表
const classList = ref([]);
const queryAllClass = async () => {
  const result = await queryAllApi();
  if (result.code) {
    classList.value = result.data;
  }
}

//表格勾选的行 -> 只保留 id
const selectedIds = ref([]);
const handleSelectionChange = (rows) => {
  selectedIds.value = rows.map(row => row.id);
}

//分页
const pageConf = ref({ currentPage: 1, pageSize: 10, total: 0 });
const handleSizeChange = (pageSize) => {
  pageConf.value.pageSize = pageSize;
  search();
}
const handleCurrentChange = (currentPage) => {
  pageConf.value.currentPage = currentPage;
  search();
}

//查询
const search = async () => {
  const result = await queryPageApi(searchForm.value.classId, searchForm.value.degree, searchForm.value.name, pageConf.value.currentPage, pageConf.value.pageSize);
  if (result.code) {
    stuList.value = result.data.rows;
    pageConf.value.total = result.data.total;
  }
}

//清空查询条件
const clearSearch = () => {
  searchForm.value = { classId: '', degree: '', name: '' };
  search();
}

//Dialog对话框
const dialogFormVisible = ref(false);
const labelWidth = ref(100);
const formTitle = ref('');
const stu = ref({
  name: '',
  studentNo: '',
  gender: '',
  phone: '',
  idCard: '',
  isCollege: '',
  address: '',
  degree: '',
  graduationDate: '',
  classId: ''
});

//重置表单数据
const resetStu = () => {
  stu.value = {
    name: '',
    studentNo: '',
    gender: '',
    phone: '',
    idCard: '',
    isCollege: '',
    address: '',
    degree: '',
    graduationDate: '',
    classId: ''
  };
}

//新增学员
const addStu = () => {
  dialogFormVisible.value = true;
  formTitle.value = '新增学员';
  resetStu();
}

//编辑学员
const edit = async (id) => {
  resetStu();
  dialogFormVisible.value = true;
  formTitle.value = '修改学员';

  const result = await queryByIdApi(id);
  if (result.code) {
    stu.value = result.data;
  }
}

//表单ref
const stuFormRef = ref();

//表单校验规则
const rules = ref({
  name: [
    { required: true, message: '姓名为必填项', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度为2-20个字', trigger: 'blur' }
  ],
  studentNo: [
    { required: true, message: '学号为必填项', trigger: 'blur' },
    { min: 10, max: 10, message: '学号长度为10个字', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '性别为必填项', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '手机号为必填项', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入合法的手机号', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '身份证号为必填项', trigger: 'blur' },
    { pattern: /^\d{18}$/, message: '请输入合法的身份证号', trigger: 'blur' }
  ],
  isCollege: [
    { required: true, message: '是否院校学员为必填项', trigger: 'change' }
  ],
  // 联系地址 / 最高学历 / 毕业时间后端都不是必填, 故不做必填校验
  classId: [
    { required: true, message: '请选择所属班级', trigger: 'change' }
  ]
});

//重置表单的校验规则-提示信息
const resetForm = (formEl) => {
  if (!formEl) return;
  formEl.resetFields();
}

//保存学员
const save = (formEl) => {
  if (!formEl) return;
  formEl.validate(async (valid) => { //valid 表示是否校验通过: true 通过 / false 不通过
    if (valid) { //通过

      let result;
      if (stu.value.id) { //修改
        result = await updateApi(stu.value);
      } else { //新增
        result = await addApi(stu.value);
      }

      if (result.code) { //成功
        ElMessage.success('操作成功');
        dialogFormVisible.value = false;
        search();
      } else { //失败
        ElMessage.error(result.msg);
      }
      return;
    }
    //不通过
    return false;
  })
}

//删除学员
const delById = async (id) => {
  ElMessageBox.confirm('您确认删除此数据吗?', '删除学生',
    { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => { //确认
    const result = await deleteByIdApi(`${id}`);
    if (result.code) {
      ElMessage.success('删除成功');
      search();
    } else {
      ElMessage.error(result.msg);
    }
  }).catch(() => { //取消
    ElMessage.info('取消删除');
  })
}

//批量删除学员
const deleteBatch = async () => {
  ElMessageBox.confirm('您确认删除此数据吗?', '删除学生',
    { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => { //确认
    const result = await deleteByIdApi(selectedIds.value.join(','));
    if (result.code) {
      ElMessage.success('删除成功');
      search();
    } else {
      ElMessage.error(result.msg);
    }
  }).catch(() => { //取消
    ElMessage.info('取消删除');
  })
}

//学员违纪扣分
const violation = (id) => {
  ElMessageBox.prompt('请输入违纪扣分', '提示',
    { confirmButtonText: '确认', cancelButtonText: '取消', inputPattern: /\d?/, inputErrorMessage: '数据无效' }
  ).then(async (res) => { //确认
    const result = await violationApi(id, res.value);
    if (result.code) {
      ElMessage.success('处理成功');
      search();
    } else {
      ElMessage.error(result.msg);
    }
  })
}
</script>

<template>
  <div>
    <div id="title">学员管理</div>
    <br/>
  </div>

  <!-- 搜索栏 -->
  <el-form inline :model="searchForm" class="demo-form-inline">
    <el-form-item label="姓名">
      <el-input v-model="searchForm.name" placeholder="请输入学生姓名"/>
    </el-form-item>
    <el-form-item label="最高学历">
      <el-select v-model="searchForm.degree" placeholder="请选择">
        <el-option v-for="item in degreeOptions" :key="item.value" :label="item.name" :value="item.value"/>
      </el-select>
    </el-form-item>
    <el-form-item label="所属班级">
      <el-select v-model="searchForm.classId" placeholder="请选择">
        <el-option v-for="item in classList" :key="item.id" :label="item.name" :value="item.id"/>
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button type="info" @click="clearSearch">清空</el-button>
    </el-form-item>
  </el-form>

  <!-- 工具栏权限一个都没有时整块不渲染(含换行), 避免只读角色留一片空白 -->
  <template v-if="hasPermission(['student:create', 'student:delete'])">
    <el-button type="success" v-permission="'student:create'" @click="addStu(); resetForm(stuFormRef)"> + 新增学员</el-button>
    <el-button type="danger" v-permission="'student:delete'" @click="deleteBatch"> - 批量删除</el-button>
    <br/>
    <br/>
  </template>

  <!-- 表格 -->
  <el-table :data="stuList" border style="width: 100%" fit @selection-change="handleSelectionChange">
    <el-table-column type="selection" align="center" width="35"/>
    <el-table-column prop="name" label="姓名" align="center" width="100"/>
    <el-table-column prop="studentNo" label="学号" align="center" width="130"/>
    <el-table-column prop="courseClassName" label="班级" align="center" width="160"/>
    <el-table-column prop="gender" label="性别" align="center" width="70">
      <template #default="scope">{{ scope.row.gender == 1 ? '男' : '女' }}</template>
    </el-table-column>
    <el-table-column prop="phone" label="手机号" align="center" width="130"/>
    <el-table-column prop="degree" label="最高学历" align="center" width="90">
      <template #default="scope">
        <span v-if="scope.row.degree == 1">初中</span>
        <span v-else-if="scope.row.degree == 2">高中</span>
        <span v-else-if="scope.row.degree == 3">大专</span>
        <span v-else-if="scope.row.degree == 4">本科</span>
        <span v-else-if="scope.row.degree == 5">硕士</span>
        <span v-else-if="scope.row.degree == 6">博士</span>
        <span v-else>其他</span>
      </template>
    </el-table-column>
    <el-table-column prop="violationCount" label="违纪次数" align="center" width="85"/>
    <el-table-column prop="violationScore" label="违纪扣分" align="center" width="85"/>
    <el-table-column prop="updateTime" label="最后修改时间" align="center" width="170"/>
    <!-- 一个操作权限都没有时整列隐藏, 避免留一列空白"操作" -->
    <el-table-column v-if="hasPermission(['student:update', 'student:violation', 'student:delete'])" label="操作" align="center">
      <template #default="scope">
        <el-button type="primary" size="small" v-permission="'student:update'" @click="edit(scope.row.id); resetForm(stuFormRef)">编辑</el-button>
        <el-button type="warning" size="small" v-permission="'student:violation'" @click="violation(scope.row.id)">违纪</el-button>
        <el-button type="danger" size="small" v-permission="'student:delete'" @click="delById(scope.row.id)">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <br/>

  <!-- 分页条 -->
  <el-pagination v-model:current-page="pageConf.currentPage" v-model:page-size="pageConf.pageSize" :page-sizes="[5, 10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pageConf.total" @size-change="handleSizeChange" @current-change="handleCurrentChange"/>

  <!-- Dialog对话框 -->
  <el-dialog v-model="dialogFormVisible" :title="formTitle" width="50%">
    <el-form :model="stu" :rules="rules" ref="stuFormRef">
      <el-row>
        <el-col :span="12">
          <el-form-item label="姓名" :label-width="labelWidth" prop="name">
            <el-input v-model="stu.name" placeholder="请输入学员姓名"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学号" :label-width="labelWidth" prop="studentNo">
            <el-input v-model="stu.studentNo" placeholder="请输入学员学号"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="性别" :label-width="labelWidth" prop="gender">
            <el-select v-model="stu.gender" placeholder="请选择" style="width: 100%">
              <el-option v-for="item in genderOptions" :key="item.value" :label="item.name" :value="item.value"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="手机号" :label-width="labelWidth" prop="phone">
            <el-input v-model="stu.phone" placeholder="请输入手机号"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="身份证号" :label-width="labelWidth" prop="idCard">
            <el-input v-model="stu.idCard" placeholder="请输入身份证号"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="是否院校" :label-width="labelWidth" prop="isCollege">
            <el-select v-model="stu.isCollege" placeholder="请选择" style="width: 100%">
              <el-option v-for="item in isCollegeOptions" :key="item.value" :label="item.name" :value="item.value"/>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="联系地址" :label-width="labelWidth" prop="address">
            <el-input v-model="stu.address" placeholder="请输入联系地址"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="最高学历" :label-width="labelWidth" prop="degree">
            <el-select v-model="stu.degree" placeholder="请选择" style="width: 100%">
              <el-option v-for="item in degreeOptions" :key="item.value" :label="item.name" :value="item.value"/>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col :span="12">
          <el-form-item label="毕业时间" :label-width="labelWidth" prop="graduationDate">
            <el-date-picker v-model="stu.graduationDate" type="date" placeholder="请选择毕业时间" value-format="YYYY-MM-DD" style="width: 100%"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所属班级" :label-width="labelWidth" prop="classId">
            <el-select v-model="stu.classId" placeholder="请选择" style="width: 100%">
              <el-option v-for="item in classList" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogFormVisible = false; resetForm(stuFormRef)">取消</el-button>
        <el-button type="primary" @click="save(stuFormRef)">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
#title {
  font-size: 20px;
  font-weight: 600;
}

.container {
  margin: 15px 0px;
}
</style>
