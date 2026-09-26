import axios from 'axios';
import { ElMessage } from 'element-plus';
import router from '../router';

// 创建axios实例对象
const request = axios.create({
  baseURL: '/api', // 开发环境由 vite 的 server.proxy 代理到 http://localhost:8080
  timeout: 600000
})

/**
 * 请求拦截器
 * 从 localStorage 取出登录信息, 把 JWT 放进请求头 token —— 后端 TokenInterceptor 读的就是它
 */
request.interceptors.request.use(
  (config) => {
    const loginUser = JSON.parse(localStorage.getItem('loginUser'));
    if (loginUser && loginUser.token) {
      config.headers.token = loginUser.token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 * 成功: 后端统一响应结构 {code, msg, data}, 这里直接把 data 层剥掉
 * 失败:
 *   401 —— 未登录/令牌过期, 提示并跳登录页
 *   403 —— 已登录但权限不足, 把后端返回的 msg(缺哪个权限)弹出来
 *   其他 —— 有 msg 就显示 msg, 否则兜底文案
 */
request.interceptors.response.use(
  (response) => {
    // 成功回调
    return response.data
  },
  (error) => {
    // 失败回调
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        ElMessage.error('登录失效, 请重新登录');
        router.push('/login');
      } else if (status === 403) {
        ElMessage.error((data && data.msg) || '权限不足, 请联系管理员!');
      } else if (data && data.msg) {
        ElMessage.error(data.msg);
      } else {
        ElMessage.error('接口访问异常');
      }
    } else {
      // 后端未启动 / 网络不通
      ElMessage.error('接口访问异常, 请确认后端服务已启动');
    }
    return Promise.reject(error)
  }
)

export default request
