/**
 * 按钮级权限指令 v-permission
 *
 * 用法(二选一):
 *   <el-button v-permission="'class:create'">新增班级</el-button>
 *   <el-button v-permission="['employee:create', 'employee:update']">上传头像</el-button>   满足其一即可
 *
 * 权限码来自登录接口下发的 loginUser.permissions, 与后端 @RequiresPermission 上的标识一致。
 *
 * ⚠️ 隐藏按钮只是体验优化, 不是安全边界。绕过前端直接调接口, 后端 PermissionInterceptor 一样会拦。
 *    因此当本地登录信息缺少 permissions 字段时这里采取「放行显示」策略, 由后端返回 403 兜底。
 */
const getPermissions = () => {
  try {
    const loginUser = JSON.parse(localStorage.getItem('loginUser'))
    if (loginUser && Array.isArray(loginUser.permissions)) {
      return loginUser.permissions
    }
  } catch (e) {
    // 本地存储被改坏, 忽略
  }
  return null
}

/**
 * 判断当前登录人是否命中所需权限
 * @param {string|string[]} value 单个权限码或权限码数组(满足其一即可)
 * @returns {boolean}
 */
export const hasPermission = (value) => {
  // 没写权限码 / 空数组 -> 不做限制
  if (!value || (Array.isArray(value) && value.length === 0)) {
    return true
  }
  const permissions = getPermissions()
  // 登录信息里没有 permissions(老数据/异常) -> 放行, 交给后端拦截
  if (permissions === null) {
    return true
  }
  const needed = Array.isArray(value) ? value : [value]
  return needed.some((code) => permissions.includes(code))
}

export default {
  mounted(el, binding) {
    if (!hasPermission(binding.value)) {
      // 用 display:none 而不是把节点摘掉, 避免破坏 el-button 外层结构
      el.style.display = 'none'
    }
  }
}
