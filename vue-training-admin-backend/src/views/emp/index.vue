<script setup>
import { ref, watch, onMounted } from 'vue';
import { queryPageApi, addApi, queryInfoApi, updateApi, deleteApi, queryRolesApi, assignRolesApi } from '@/api/emp';
import { queryAllApi as queryAllDeptApi } from '@/api/dept';
import { queryAllApi as queryAllRoleApi } from '@/api/role';
import { uploadUrl } from '@/api/file';
import { ElMessage, ElMessageBox } from 'element-plus'
import { hasPermission } from '@/utils/permission'

//token  —— el-upload 自己发请求, 不会走 axios 拦截器, 所以要手动带上请求头
const token = ref('');

//元数据
//职位列表数据(与后端 Employee 的 position 枚举一致)
const jobs = ref([{ name: '班主任', value: 1 },{ name: '讲师', value: 2 },{ name: '学工主管', value: 3 },{ name: '教研主管', value: 4 },{ name: '咨询师', value: 5 },{ name: '其他', value: 6 }])
//性别列表数据
const genders = ref([{ name: '男', value: 1 }, { name: '女', value: 2 }])
//部门列表数据
const depts = ref([])
//角色列表数据(分配角色用)
const roles = ref([])

//搜索表单对象
const searchEmp = ref({name: '', gender: '', date: [], begin: '', end: ''})

//侦听searchEmp的date属性
watch(() => searchEmp.value.date, (newVal, oldVal) => {
  // 注意 newVal 可能是 null(清除日期), 不能直接取 length
  if(newVal && newVal.length == 2){
    searchEmp.value.begin = newVal[0];
    searchEmp.value.end = newVal[1];
  }else {
    searchEmp.value.begin = '';
    searchEmp.value.end = '';
  }
})

//钩子函数
onMounted(() => {
  search(); //查询员工列表数据
  queryAllDepts();//查询所有部门列表数据
  getToken(); //获取token
})

//获取token
const getToken = () => {
  const loginUser = JSON.parse(localStorage.getItem('loginUser'));
  if(loginUser && loginUser.token){
    token.value = loginUser.token;
  }
}

//查询所有部门数据
const queryAllDepts = async () => {
  const result = await queryAllDeptApi();
  if(result.code){
    depts.value = result.data;
  }
}

//查询员工列表
const search = async () => {
  const result = await queryPageApi(searchEmp.value.name, searchEmp.value.gender,
                         searchEmp.value.begin, searchEmp.value.end, currentPage.value, pageSize.value);
  if(result.code){
    empList.value = result.data.rows;
    total.value = result.data.total;
  }
}

//清空
const clear = () => {
  searchEmp.value = {name: '', gender: '', date: [], begin: '', end: ''};
  search();
}

//员工列表数据
const empList = ref([])

//分页
const currentPage = ref(1); //页码
const pageSize = ref(10); //每页展示记录数
const total = ref(0); //总记录数

//每页展示记录数变化
const handleSizeChange = (val) => {
  search();
}
//页码变化时触发
const handleCurrentChange = (val) => {
  search();
}

//职位 -> 中文
const positionName = (position) => {
  switch(position){
    case 1: return '班主任';
    case 2: return '讲师';
    case 3: return '学工主管';
    case 4: return '教研主管';
    case 5: return '咨询师';
    default: return '其他';
  }
}

//空表单
const emptyEmployee = () => ({
  username: '',
  name: '',
  gender: '',
  phone: '',
  position: '',
  salary: '',
  departmentId: '',
  entryDate: '',
  avatar: '',
  experienceList: []
})

//新增员工
const addEmp = () => {
  dialogVisible.value = true;
  dialogTitle.value = '新增员工';
  employee.value = emptyEmployee();

  //重置表单的校验规则-提示信息
  if (empFormRef.value){
    empFormRef.value.resetFields();
  }
}

//新增/修改表单
const employee = ref(emptyEmployee())

// 控制弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('新增员工')

// 文件上传 —— 后端接口 POST /files/upload, 返回 {code, data: url}
// 图片上传成功后触发
const handleAvatarSuccess = (response) => {
  employee.value.avatar = response.data;
}
// 文件上传之前触发(后端还会再校验一次)
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
    ElMessage.error('只支持上传图片')
    return false
  } else if (rawFile.size / 1024 / 1024 > 10) {
    ElMessage.error('只能上传10M以内图片')
    return false
  }
  return true
}

//添加工作经历
const addExprItem = () => {
  employee.value.experienceList.push({company: '', position: '', beginDate: '', endDate: '', exprDate: []});
}
//删除工作经历
const delExprItem = (index) => {
  employee.value.experienceList.splice(index,1);
}
//侦听-employee员工对象中的工作经历信息: 把日期区间拆成 beginDate / endDate
watch(() => employee.value.experienceList, (newVal, oldVal) => {
  if(employee.value.experienceList && employee.value.experienceList.length > 0){
    employee.value.experienceList.forEach((expr) => {
      if(expr.exprDate && expr.exprDate.length == 2){
        expr.beginDate = expr.exprDate[0];
        expr.endDate = expr.exprDate[1];
      }
    })
  }
}, {deep: true}) //深度侦听

//保存员工
const save = async () => {
  //表单校验
  if(!empFormRef.value) return;
  empFormRef.value.validate(async (valid) => { //valid 表示是否校验通过: true 通过 / false 不通过
    if(valid){ //通过

      let result;
      if(employee.value.id){ //修改
        result = await updateApi(employee.value);
      }else { //新增
        result = await addApi(employee.value);
      }

      if(result.code) {//成功
        ElMessage.success('保存成功');
        dialogVisible.value = false;
        search();
      }else { //失败了
        ElMessage.error(result.msg);
      }
    }else { //不通过
      ElMessage.error('表单校验不通过');
    }
  })
}
//表单引用
const empFormRef = ref();

//表单校验规则
const rules = ref({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度应在2到20个字符之间', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度应在2到10个字符之间', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    // 后端 Employee 的手机号规则就是 ^1\d{10}$
    { pattern: /^1\d{10}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ]
});

//编辑
const edit = async (id) => {
  const result = await queryInfoApi(id);
  if(result.code){
    dialogVisible.value = true;
    dialogTitle.value = '修改员工';
    employee.value = result.data;

    //对工作经历进行处理: 后端返回 beginDate / endDate, 表单用日期区间
    let experienceList = employee.value.experienceList;
    if(experienceList && experienceList.length > 0){
      experienceList.forEach((expr) => {
        expr.exprDate = [expr.beginDate, expr.endDate];
      })
    }
  }
}

//删除员工
const deleteById = (id) => {
  //弹出确认框
  ElMessageBox.confirm('确定删除该员工吗?','提示',
    { confirmButtonText: '确定',cancelButtonText: '取消',type: 'warning'}
  ).then(async () => { //确认
    const result = await deleteApi(id);
    if(result.code){
      ElMessage.success('删除员工成功');
      search();
    }else{
      ElMessage.error(result.msg);
    }
  }).catch(() => {}) //取消
}
//记录勾选的员工的id
const selectedIds = ref([]);
//复选框勾选发生变化时触发 - selection: 当前选中的记录 (数组)
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map( item => item.id);
}

//批量删除
const deleteByIds = () => {
  //弹出确认框
  ElMessageBox.confirm('确定删除选中员工吗?','提示',
    { confirmButtonText: '确定',cancelButtonText: '取消',type: 'warning'}
  ).then(async () => { //确认
    const result = await deleteApi(selectedIds.value);
    if(result.code){
      ElMessage.success('删除员工成功');
      search();
    }else{
      ElMessage.error(result.msg);
    }
  }).catch(() => {}) //取消
}

// ------------------------- 分配角色(后端新增能力) -------------------------
const roleDialogVisible = ref(false);
const currentEmp = ref({});
const selectedRoleIds = ref([]);

//打开分配角色对话框: 先查出该员工已有的角色ID
const assignRole = async (row) => {
  currentEmp.value = row;
  selectedRoleIds.value = [];

  //角色列表按需加载(避免没有 role:list 权限时干扰员工列表)
  if(!roles.value.length){
    const roleResult = await queryAllRoleApi();
    if(roleResult.code){
      roles.value = roleResult.data;
    }
  }

  const result = await queryRolesApi(row.id);
  if(result.code){
    selectedRoleIds.value = result.data || [];
  }
  roleDialogVisible.value = true;
}

//保存员工角色(全量覆盖)
const saveRoles = async () => {
  const result = await assignRolesApi(currentEmp.value.id, selectedRoleIds.value);
  if(result.code){
    ElMessage.success('分配角色成功');
    roleDialogVisible.value = false;
  }else{
    ElMessage.error(result.msg);
  }
}

</script>

<template>
  <h1>员工管理</h1>

  <!-- 搜索栏 -->
  <el-form :inline="true" :model="searchEmp" class="demo-form-inline">
    <el-form-item label="姓名">
      <el-input v-model="searchEmp.name" placeholder="请输入员工姓名" />
    </el-form-item>

    <el-form-item label="性别">
      <el-select v-model="searchEmp.gender" placeholder="请选择">
        <el-option label="男" value="1" />
        <el-option label="女" value="2" />
      </el-select>
    </el-form-item>

    <el-form-item label="入职日期">
      <el-date-picker
        v-model="searchEmp.date"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
      />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="clear">清空</el-button>
    </el-form-item>
  </el-form>

  <!-- 功能按钮: 工具栏权限一个都没有时整块不渲染(含换行), 避免只读角色留一片空白 -->
  <template v-if="hasPermission(['employee:create', 'employee:delete'])">
    <el-button type="primary" @click="addEmp" v-permission="'employee:create'"> + 新增员工</el-button>
    <el-button type="danger" @click="deleteByIds" v-permission="'employee:delete'"> - 批量删除</el-button>
    <br>
    <br>
  </template>

  <!-- 数据展示表格 -->
  <el-table :data="empList" border style="width: 100%" @selection-change="handleSelectionChange">
    <el-table-column type="selection" width="55" align="center"/>
    <el-table-column prop="name" label="姓名" width="120" align="center"/>
    <el-table-column label="性别" width="80" align="center">
      <template #default="scope">
        {{ scope.row.gender == 1 ? '男' : '女' }}
      </template>
    </el-table-column>
    <el-table-column label="头像" width="170" align="center">
      <template #default="scope">
        <img :src="scope.row.avatar" alt="Avatar" class="avatar">
      </template>
    </el-table-column>
    <el-table-column prop="departmentName" label="部门名称" width="170" align="center"/>
    <el-table-column label="职位" width="120" align="center">
      <template #default="scope">
        {{ positionName(scope.row.position) }}
      </template>
    </el-table-column>
    <el-table-column prop="entryDate" label="入职日期" width="180" align="center"/>
    <el-table-column prop="updateTime" label="最后操作时间" width="210" align="center"/>
    <!-- 一个操作权限都没有时整列隐藏, 避免留一列空白"操作" -->
    <el-table-column v-if="hasPermission(['employee:update', 'employee:assign-role', 'employee:delete'])" label="操作" fixed="right" align="center">
      <template #default="scope">
        <el-button size="small" type="primary" @click="edit(scope.row.id)" v-permission="'employee:update'">编辑</el-button>
        <el-button size="small" type="warning" @click="assignRole(scope.row)" v-permission="'employee:assign-role'">分配角色</el-button>
        <el-button size="small" type="danger" @click="deleteById(scope.row.id)" v-permission="'employee:delete'">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <br>

  <!-- 分页条 -->
  <el-pagination
    v-model:current-page="currentPage"
    v-model:page-size="pageSize"
    :page-sizes="[10, 20, 30, 40]"
    layout="total, sizes, prev, pager, next, jumper"
    :total="total"
    @size-change="handleSizeChange"
    @current-change="handleCurrentChange"
  />

  <!-- 新增员工/修改员工的对话框 -->
  <el-dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form :model="employee" :rules="rules" ref="empFormRef" label-width="80px">
      <!-- 第一行 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="employee.username" placeholder="请输入员工用户名，2-20个字"></el-input>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="姓名" prop="name">
            <el-input v-model="employee.name" placeholder="请输入员工姓名，2-10个字"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 第二行 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="性别" prop="gender">
            <el-select v-model="employee.gender" placeholder="请选择性别" style="width: 100%;">
              <el-option v-for="g in genders" :key="g.value" :label="g.name" :value="g.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="employee.phone" placeholder="请输入员工手机号"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 第三行 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="职位">
            <el-select v-model="employee.position" placeholder="请选择职位" style="width: 100%;">
              <el-option v-for="j in jobs" :key="j.value" :label="j.name" :value="j.value"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="薪资">
            <el-input v-model="employee.salary" placeholder="请输入员工薪资"></el-input>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 第四行 -->
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="所属部门">
            <el-select v-model="employee.departmentId" placeholder="请选择部门" style="width: 100%;">
              <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="入职日期">
            <el-date-picker v-model="employee.entryDate" type="date" style="width: 100%;" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD"></el-date-picker>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 第五行 -->
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="头像">
            <!-- 后端接口: POST /files/upload, 字段名 file -->
            <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="{'token': token}"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              >
              <img v-if="employee.avatar" :src="employee.avatar" class="avatar" alt="Avatar" />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 工作经历 -->
      <el-row :gutter="10">
        <el-col :span="24">
          <el-form-item label="工作经历">
            <el-button type="success" size="small" @click="addExprItem">+ 添加工作经历</el-button>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="3" v-for="(expr,index) in employee.experienceList" :key="index">
        <el-col :span="10">
          <el-form-item size="small" label="时间" label-width="80px">
            <el-date-picker type="daterange" v-model="expr.exprDate" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" ></el-date-picker>
          </el-form-item>
        </el-col>

        <el-col :span="6">
          <el-form-item size="small" label="公司" label-width="60px">
            <el-input placeholder="请输入公司名称" v-model="expr.company"></el-input>
          </el-form-item>
        </el-col>

        <el-col :span="6">
          <el-form-item size="small" label="职位" label-width="60px">
            <el-input placeholder="请输入职位" v-model="expr.position"></el-input>
          </el-form-item>
        </el-col>

        <el-col :span="2">
          <el-form-item size="small" label-width="0px">
            <el-button type="danger" @click="delExprItem(index)">- 删除</el-button>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <!-- 底部按钮 -->
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 分配角色对话框(后端新增接口 PUT /employees/{id}/roles) -->
  <el-dialog v-model="roleDialogVisible" title="分配角色" width="30%">
    <el-form label-width="80px">
      <el-form-item label="员工">
        {{ currentEmp.name }}
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="selectedRoleIds" multiple placeholder="请选择角色" style="width: 100%;">
          <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRoles">保存</el-button>
      </span>
    </template>
  </el-dialog>

</template>

<style scoped>
.avatar {
  height: 40px;
}
.avatar-uploader .avatar {
  width: 78px;
  height: 78px;
  display: block;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 78px;
  height: 78px;
  text-align: center;
  border-radius: 10px;
  /* 添加灰色的虚线边框 */
  border: 1px dashed var(--el-border-color);
}
</style>
