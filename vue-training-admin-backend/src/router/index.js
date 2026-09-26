import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

import IndexView from '@/views/index/index.vue'
import ClazzView from '@/views/clazz/index.vue'
import StuView from '@/views/stu/index.vue'
import DeptView from '@/views/dept/index.vue'
import EmpView from '@/views/emp/index.vue'
import RoleView from '@/views/role/index.vue'
import LogView from '@/views/log/index.vue'
import EmpReportView from '@/views/report/emp/index.vue'
import StuReportView from '@/views/report/stu/index.vue'
import LayoutView from '@/views/layout/index.vue'
import LoginView from '@/views/login/index.vue'

/**
 * 路由表
 * 注意 path 必须与数据库 menu 表里的 path 完全一致:
 * 侧边栏菜单是按 /menus/mine 下发的 path 渲染的, 路由守卫也按它判断能否进入
 *   /index  首页            menu.permission_code = menu:index
 *   /clazz  班级管理         class:list
 *   /stu    学员管理         student:list
 *   /dept   部门管理         department:list
 *   /emp    员工管理         employee:list
 *   /report/emp 员工信息统计  report:employee
 *   /report/stu 学员信息统计  report:student
 *   /log    日志信息统计      report:log
 *   /role   角色管理         role:list
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'layout',
      component: LayoutView,
      redirect: '/index', // 重定向
      children: [
        { path: '/index', name: 'index', component: IndexView },
        { path: '/clazz', name: 'clazz', component: ClazzView },
        { path: '/stu', name: 'stu', component: StuView },
        { path: '/dept', name: 'dept', component: DeptView },
        { path: '/emp', name: 'emp', component: EmpView },
        { path: '/report/emp', name: 'empReport', component: EmpReportView },
        { path: '/report/stu', name: 'stuReport', component: StuReportView },
        { path: '/log', name: 'log', component: LogView },
        { path: '/role', name: 'role', component: RoleView }
      ]
    },
    { path: '/login', name: 'login', component: LoginView }
  ]
})

/**
 * 全局前置守卫
 * 只做「体验优化」: 未登录跳登录页; 已登录时按登录接口下发的 menus 判断能否进入某页面。
 * 真正的安全边界在后端 TokenInterceptor / PermissionInterceptor, 绕过前端一样会被拦。
 */
router.beforeEach((to, from) => {
  // 登录页直接放行
  if (to.path === '/login') {
    return true
  }

  let loginUser = null
  try {
    loginUser = JSON.parse(localStorage.getItem('loginUser'))
  } catch (e) {
    loginUser = null
  }

  // 未登录 -> 登录页
  if (!loginUser) {
    return '/login'
  }

  // 登录信息里没有菜单(异常数据) -> 不做限制, 交给后端拦
  const menus = loginUser.menus
  if (!menus || !menus.length) {
    return true
  }

  const paths = menus.map((menu) => menu.path)
  if (to.path === '/' || to.path === '/index' || paths.indexOf(to.path) >= 0) {
    return true
  }

  ElMessage.error('没有访问该页面的权限!')
  return '/index'
})

export default router
