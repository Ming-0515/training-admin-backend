<script setup>
import { ref, onMounted } from 'vue';
import { ElMessageBox, ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { queryMineApi } from '@/api/menu';

const router = useRouter();

//当前登录员工姓名
const loginName = ref('');

//侧边栏菜单(树形: 分组 -> 菜单项)
const menuList = ref([]);

/**
 * 数据库 menu 表只有 name/path, 没有图标字段, 所以图标在前端按路由维护。
 * 新增页面时在这里补一行即可, 不影响后端。
 */
const menuIcon = {
  '/index': 'Promotion',
  '/clazz': 'HomeFilled',
  '/stu': 'UserFilled',
  '/dept': 'HelpFilled',
  '/emp': 'Avatar',
  '/report/emp': 'InfoFilled',
  '/report/stu': 'Share',
  '/log': 'Document',
  '/role': 'Lock'
};
const groupIcon = {
  '班级学员管理': 'Menu',
  '系统信息管理': 'Tools',
  '数据统计管理': 'Histogram',
  '权限管理': 'Setting'
};

/**
 * 兜底菜单: 登录信息里没有 menus 且 /menus/mine 也拿不到时使用, 避免侧边栏空白。
 * 正常情况下用不到 —— 登录接口会下发 menus。
 */
const fallbackMenus = [
  { id: 2, parentId: 0, name: '班级学员管理', sort: 2, path: null },
  { id: 3, parentId: 2, name: '班级管理', sort: 1, path: '/clazz' },
  { id: 7, parentId: 2, name: '学员管理', sort: 2, path: '/stu' },
  { id: 12, parentId: 0, name: '系统信息管理', sort: 3, path: null },
  { id: 13, parentId: 12, name: '部门管理', sort: 1, path: '/dept' },
  { id: 17, parentId: 12, name: '员工管理', sort: 2, path: '/emp' },
  { id: 22, parentId: 0, name: '数据统计管理', sort: 4, path: null },
  { id: 23, parentId: 22, name: '员工信息统计', sort: 1, path: '/report/emp' },
  { id: 24, parentId: 22, name: '学员信息统计', sort: 2, path: '/report/stu' },
  { id: 25, parentId: 22, name: '日志信息统计', sort: 3, path: '/log' }
];

/**
 * 把 /menus/mine 返回的扁平菜单列表组装成树。
 * 规则: parentId 为 0 的是顶级节点; 顶级节点自己有 path 就是一级菜单(如"首页"),
 *      没有 path 就是分组, 它的子节点才是可点击的菜单项。
 * 分组下没有任何可见子菜单时, 整个分组不显示。
 */
const buildMenuTree = (list) => {
  const bySort = (a, b) => (a.sort || 0) - (b.sort || 0);
  const ids = list.map((menu) => menu.id);
  // 顶级节点: parentId 为空/0, 或者父节点本身不在可见列表里
  // (角色只授权了子菜单、没授权分组时会这样, 此时把子菜单提上来显示, 避免菜单整体丢失)
  const isRoot = (menu) => !menu.parentId || ids.indexOf(menu.parentId) < 0;
  return list
    .filter(isRoot)
    .sort(bySort)
    .map((root) => ({
      ...root,
      children: list.filter((menu) => menu.parentId === root.id).sort(bySort)
    }))
    // 既没有 path 又没有子菜单的分组不渲染
    .filter((node) => node.path || node.children.length > 0);
};

//加载菜单: 先用登录时下发的 menus 立即渲染, 再请求 /menus/mine 刷新(角色变更后无需重新登录)
const loadMenus = async () => {
  let menus = [];
  try {
    const loginUser = JSON.parse(localStorage.getItem('loginUser'));
    if (loginUser) {
      loginName.value = loginUser.name || loginUser.username || '';
      menus = loginUser.menus || [];
    }
  } catch (e) {
    menus = [];
  }

  if (menus.length) {
    menuList.value = buildMenuTree(menus);
  }

  try {
    const result = await queryMineApi();
    if (result.code && result.data && result.data.length) {
      menus = result.data;
      menuList.value = buildMenuTree(menus);
      // 同步回本地, 让路由守卫也用最新的菜单
      const loginUser = JSON.parse(localStorage.getItem('loginUser'));
      if (loginUser) {
        loginUser.menus = menus;
        localStorage.setItem('loginUser', JSON.stringify(loginUser));
      }
    }
  } catch (e) {
    // /menus/mine 失败不影响使用, 继续用登录时下发的菜单
  }

  if (!menuList.value.length) {
    menuList.value = buildMenuTree(fallbackMenus);
  }
};

//钩子函数
onMounted(() => {
  loadMenus();
});

//退出登录
const logout = () => {
  //弹出确认框
  ElMessageBox.confirm('确认退出登录吗?','提示',
    { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'}
  ).then(async () => { //确认
    ElMessage.success('退出登录成功');
    localStorage.removeItem('loginUser');
    //跳转页面-登录
    router.push('/login');
  }).catch(() => { //取消
    ElMessage.info('已取消退出登录');
  })
}
</script>

<template>
  <div class="common-layout">
    <el-container>
      <!-- Header 区域 -->
      <el-header class="header">
        <span class="title">培训机构后台管理系统</span>
        <span class="right_tool">
          <a href="javascript:;" @click="logout">
            <el-icon><SwitchButton /></el-icon> 退出登录 【{{loginName}}】
          </a>
        </span>
      </el-header>

      <el-container>
        <!-- 左侧菜单: 按 /menus/mine 动态渲染, 受权限限制的菜单不会出现 -->
        <el-aside width="200px" class="aside">
          <el-menu router :default-active="$route.path">
            <template v-for="node in menuList" :key="node.id">
              <!-- 分组菜单 -->
              <el-sub-menu v-if="node.children && node.children.length" :index="String(node.id)">
                <template #title>
                  <el-icon><component :is="groupIcon[node.name] || 'Menu'" /></el-icon> {{ node.name }}
                </template>
                <el-menu-item v-for="child in node.children" :key="child.id" :index="child.path">
                  <el-icon><component :is="menuIcon[child.path] || 'Document'" /></el-icon>{{ child.name }}
                </el-menu-item>
              </el-sub-menu>

              <!-- 一级菜单(首页) -->
              <el-menu-item v-else :index="node.path">
                <el-icon><component :is="menuIcon[node.path] || 'Document'" /></el-icon> {{ node.name }}
              </el-menu-item>
            </template>
          </el-menu>
        </el-aside>

        <el-main>
          <router-view></router-view>
        </el-main>
      </el-container>

    </el-container>
  </div>
</template>

<style scoped>
.header {
  background-image: linear-gradient(to right, #00547d, #007fa4, #00aaa0, #00d072, #a8eb12);
}

.title {
  color: white;
  font-size: 40px;
  font-family: 楷体;
  line-height: 60px;
  font-weight: bolder;
}

.right_tool{
  float: right;
  line-height: 60px;
}

a {
  color: white;
  text-decoration: none;
}

.aside {
  width: 220px;
  border-right: 1px solid #ccc;
  height: 730px;
}
</style>
