<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { hasPermission } from '@/utils/permission';
import { queryAllApi, addApi, updateApi, deleteByIdApi, queryMenuIdsApi, assignMenusApi } from '@/api/role';
import { queryAllApi as queryAllMenuApi } from '@/api/menu';

/**
 * 角色管理(后端 RBAC 新增能力)
 * 对应接口: GET /roles、POST /roles、PUT /roles、DELETE /roles/{id}
 *          GET /roles/{id}/menus、PUT /roles/{id}/menus、GET /menus
 * 权限码:   role:list / role:create / role:update / role:delete / role:assign-menu
 */

//钩子函数
onMounted(() => {
  search();
  queryMenuTree();
})

//角色列表数据
const roleList = ref([])
const search = async () => {
  const result = await queryAllApi();
  if(result.code){
    roleList.value = result.data;
  }
}

//权限树数据(来自 GET /menus, 后端已组装好 children)
const menuTree = ref([])
const menuTreeProps = { label: 'name', children: 'children' }
const queryMenuTree = async () => {
  const result = await queryAllMenuApi();
  if(result.code){
    menuTree.value = result.data;
  }
}

//是否是内置超级管理员(标识不允许改, 也不允许删除)
const isAdmin = (row) => row && row.code === 'ADMIN';

// ---------------------------- 新增 / 修改角色 ----------------------------
const dialogVisible = ref(false);
const formTitle = ref('');
const roleForm = ref({id: null, name: '', code: '', remark: ''});
const roleFormRef = ref(null);

//表单校验规则(与后端约束一致: 名称 <=20, 标识 <=30)
const rules = ref({
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度为2-20个字', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色标识', trigger: 'blur' },
    { min: 2, max: 30, message: '角色标识长度为2-30个字', trigger: 'blur' }
  ]
})

//打开对话框后清掉上一次的校验提示(不能用 resetFields, 否则会把刚填入的值重置掉)
const clearValidate = () => {
  nextTick(() => {
    if(roleFormRef.value){
      roleFormRef.value.clearValidate();
    }
  })
}

//新增角色
const addRole = () => {
  formTitle.value = '新增角色';
  roleForm.value = {id: null, name: '', code: '', remark: ''};
  dialogVisible.value = true;
  clearValidate();
}

//修改角色
const editRole = (row) => {
  formTitle.value = '修改角色';
  roleForm.value = {id: row.id, name: row.name, code: row.code, remark: row.remark};
  dialogVisible.value = true;
  clearValidate();
}

//保存角色
const save = async () => {
  if(!roleFormRef.value) return;
  roleFormRef.value.validate(async (valid) => {
    if(!valid) return;

    let result;
    if(roleForm.value.id){ //修改
      result = await updateApi(roleForm.value);
    }else{ //新增
      result = await addApi(roleForm.value);
    }

    if(result.code){
      ElMessage.success('操作成功');
      dialogVisible.value = false;
      search();
    }else{
      ElMessage.error(result.msg);
    }
  })
}

//删除角色
const deleteRole = (row) => {
  ElMessageBox.confirm(`此操作将永久删除角色【${row.name}】, 是否继续?`, '提示',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
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

// ---------------------------- 分配权限 ----------------------------
const permDialogVisible = ref(false);
const permRole = ref({});
const permTreeRef = ref(null);

//打开分配权限对话框
const assignMenu = async (row) => {
  permRole.value = row;
  permDialogVisible.value = true;

  //查出该角色已勾选的权限ID
  const result = await queryMenuIdsApi(row.id);
  const checkedKeys = (result.code && result.data) ? result.data : [];

  //等树渲染完再回显勾选状态
  nextTick(() => {
    if(permTreeRef.value){
      permTreeRef.value.setCheckedKeys(checkedKeys);
    }
  });
}

//保存角色权限(全量覆盖)
const saveMenus = async () => {
  if(!permTreeRef.value) return;

  // 勾选的叶子节点 + 半选的父分组
  // 分组菜单(如"权限管理")本身也是 menu 表里的一行, 不一起授权的话
  // /menus/mine 里就不会出现这个分组, 前端侧边栏会少了这一组
  const checkedKeys = permTreeRef.value.getCheckedKeys();
  const halfCheckedKeys = permTreeRef.value.getHalfCheckedKeys();
  const menuIds = checkedKeys.concat(halfCheckedKeys);

  const result = await assignMenusApi(permRole.value.id, menuIds);
  if(result.code){
    ElMessage.success('分配权限成功');
    permDialogVisible.value = false;
  }else{
    ElMessage.error(result.msg);
  }
}
</script>

<template>
  <h1>角色管理</h1>
  <!-- 工具栏权限一个都没有时整块不渲染(含换行), 避免只读角色留一片空白 -->
  <template v-if="hasPermission(['role:create'])">
    <el-button type="primary" @click="addRole" style="float: right;" v-permission="'role:create'"> + 新增角色</el-button>
    <br>
    <br>
  </template>

  <!-- 表格 -->
  <el-table :data="roleList" border style="width: 100%">
    <el-table-column type="index" label="序号" width="80" align="center"/>
    <el-table-column prop="name" label="角色名称" width="180" align="center"/>
    <el-table-column prop="code" label="角色标识" width="220" align="center"/>
    <el-table-column prop="remark" label="备注" align="center"/>
    <el-table-column prop="updateTime" label="最后修改时间" width="200" align="center"/>
    <!-- 一个操作权限都没有时整列隐藏, 避免留一列空白"操作" -->
    <el-table-column v-if="hasPermission(['role:update', 'role:assign-menu', 'role:delete'])" fixed="right" label="操作" width="280" align="center">
      <template #default="row">
        <el-button size="small" type="primary" @click="editRole(row.row)" v-permission="'role:update'">修改</el-button>
        <el-button size="small" type="success" @click="assignMenu(row.row)" v-permission="'role:assign-menu'">分配权限</el-button>
        <el-button size="small" type="danger" :disabled="isAdmin(row.row)" @click="deleteRole(row.row)" v-permission="'role:delete'">删除</el-button>
      </template>
    </el-table-column>
  </el-table>

  <!-- 新增/修改角色对话框 -->
  <el-dialog v-model="dialogVisible" :title="formTitle" width="30%">
    <el-form :model="roleForm" :rules="rules" ref="roleFormRef">
      <el-form-item label="角色名称" prop="name" label-width="80px">
        <el-input v-model="roleForm.name" placeholder="请输入角色名称" autocomplete="off"/>
      </el-form-item>
      <el-form-item label="角色标识" prop="code" label-width="80px">
        <!-- 内置超级管理员的标识不允许修改(后端也会拦) -->
        <el-input v-model="roleForm.code" placeholder="如 AUDITOR" autocomplete="off"
                  :disabled="roleForm.id != null && isAdmin(roleForm)"/>
      </el-form-item>
      <el-form-item label="备注" prop="remark" label-width="80px">
        <el-input v-model="roleForm.remark" type="textarea" :rows="3" placeholder="请输入备注"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 分配权限对话框 -->
  <el-dialog v-model="permDialogVisible" width="35%" :title="`分配权限 - ${permRole.name || ''}`">
    <el-alert type="info" :closable="false" show-icon
              title="勾选该角色可以访问的菜单与可执行的操作, 保存后立即生效(员工无需重新登录)。"/>
    <br>
    <el-tree
      ref="permTreeRef"
      :data="menuTree"
      :props="menuTreeProps"
      node-key="id"
      show-checkbox
      default-expand-all
    />
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMenus">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>
