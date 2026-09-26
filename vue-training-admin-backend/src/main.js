import { createApp } from 'vue'

import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import './assets/main.css'
import permission from './utils/permission'

const app = createApp(App)

app.use(router)
app.use(ElementPlus, {locale: zhCn})

// 全局注册 Element Plus 图标(侧边栏菜单会按名字动态渲染图标)
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 按钮级权限指令: v-permission="'class:create'"
app.directive('permission', permission)

app.mount('#app')
