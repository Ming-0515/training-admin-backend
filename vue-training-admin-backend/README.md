# vue-training-admin-backend

培训机构后台管理系统的前端工程（Vue 3 + Vite + Element Plus + ECharts）。

独立的 Vue 3 前端工程，用 Vite 开发与构建，开发时把 `/api` 代理到后端。

- 后端：`../training-admin-backend`（Spring Boot，监听 8080）
- 接口文档：`../training-admin-backend/docs/API.md`
- 演示账号：`zhangwei / 123456`（超级管理员，全部权限）

---

## 1. 启动

### 方式一：VS Code 启动（推荐）

打开仓库根目录 `training-admin-backend`，按 `F5` 或打开「运行和调试」面板，选择：

| 配置 | 作用 |
|---|---|
| **前端：dev 启动 vite** | 跑 vite 并自动打开浏览器 `http://localhost:5173/`。**平时点这个就行** |
| **前端：Chrome 调试（需先启动 dev）** | 想打断点时用：先让上一个配置跑起来，再启动它，断点会映射到 `src/` |

后端另开一个终端跑 `mvn spring-boot:run`，或用「终端 → 运行任务」里的 `后端: mvn spring-boot:run`。

> 改完 `launch.json` / `tasks.json` 后如果运行下拉框里还看不到新配置，
> 执行 `Ctrl+Shift+P` → `Developer: Reload Window` 重载一次窗口。

**⚠️ 如果你点「运行」后终端报 `表达式或语句中包含意外的标记"-Command"`**

这不是项目的问题，是 VS Code **用户级** `settings.json` 里 PowerShell 终端 profile 的写法导致的：

```jsonc
"terminal.integrated.profiles.windows": {
    "PowerShell": {
        "path": "...\\powershell.exe",
        "args": ["-NoExit", "-Command", "[Console]::OutputEncoding = ..."]   // ← 元凶
    }
}
```

VS Code 跑任务/脚本时会**再拼一条自己的 `-Command`**，于是实际执行的是
`powershell -NoExit -Command <UTF8设置> -Command npm run dev`，
PowerShell 把第一个 `-Command` 之后的全部内容当脚本正文，第二个 `-Command` 就成了非法标记。

本项目已经在 `.vscode/settings.json` 里用 `terminal.integrated.automationProfile.windows`
给「任务/自动化」单独指定了不带 `args` 的干净 PowerShell 来绕开它；
同时 `tasks.json` 里的 `前端: dev` 任务用 `type: "process"` 直接起 node，**根本不经过 shell**，最稳妥。

彻底修复（对所有项目有效，做完就可以删掉本项目的 `.vscode/settings.json`）：

1. 把编码设置挪进 PowerShell 配置文件 `$PROFILE`
   （`C:\Users\<你>\Documents\WindowsPowerShell\Microsoft.PowerShell_profile.ps1`）：
   ```powershell
   [Console]::OutputEncoding = [System.Text.Encoding]::UTF8
   [Console]::InputEncoding  = [System.Text.Encoding]::UTF8
   $OutputEncoding = [System.Text.Encoding]::UTF8
   ```
2. 把用户 `settings.json` 里 PowerShell profile 的 `"args"` 整段删掉。

> 只启前端、不启后端时页面能打开，但登录会提示「接口访问异常, 请确认后端服务已启动」。

### 方式二：终端启动

```bash
cd vue-training-admin-backend
npm install          # 首次
npm run dev          # 启动开发服务器, 并自动打开 http://localhost:5173
npm run build        # 打包到 dist/
npm run preview      # 本地预览构建产物
```

---

## 2. 接口是怎么连上后端的

前端所有请求都以 `/api` 开头，由 `vite.config.js` 里的 `server.proxy` 转发到 `http://localhost:8080`
（转发时会去掉 `/api` 前缀）：

| 前端请求 | 实际转发到后端 |
|---|---|
| `/api/login` | `POST http://localhost:8080/login` |
| `/api/employees?page=1` | `GET http://localhost:8080/employees?page=1` |
| `/api/files/upload` | `POST http://localhost:8080/files/upload` |
| `/api/uploads/avatar/photo.jpg` | `GET http://localhost:8080/uploads/avatar/photo.jpg` |

后端上传接口返回的地址带 `file.storage.access-prefix`（默认 `/api/uploads`），正好对应上表最后一行，
所以 `<img :src="row.avatar">` 不需要额外处理。

> **不需要改后端配置**：`access-prefix` 保持 `/api/uploads` 即可，Vite 代理会把 `/api` 去掉再转给后端。

`npm run build` 会把产物输出到 `dist/`，`npm run preview` 可在本地预览该产物。

---

## 3. 目录结构

```
vue-training-admin-backend/
├── index.html                  入口 HTML(标题: 培训机构后台管理系统)
├── vite.config.js              别名 @ -> src, dev 端口 5173, /api 代理
├── .vscode/                    launch.json / tasks.json (F5 直接启动)
└── src/
    ├── main.js                 注册 Element Plus(中文)、全部图标、v-permission 指令
    ├── App.vue
    ├── assets/                 背景图、首页大图、全局 css
    ├── utils/
    │   ├── request.js          axios 实例: 注入 token, 统一处理 401 / 403
    │   └── permission.js       按钮级权限指令 v-permission
    ├── api/                    按模块拆分的接口封装(与后端 Controller 一一对应)
    │   ├── login.js  clazz.js  stu.js  dept.js  emp.js
    │   └── report.js log.js    role.js menu.js file.js
    ├── router/index.js         路由表 + 菜单权限守卫
    └── views/
        ├── layout/index.vue    顶部栏 + 按 /menus/mine 动态渲染的侧边栏
        ├── login/index.vue
        ├── index/index.vue     首页
        ├── clazz/  stu/        班级管理 / 学员管理
        ├── dept/   emp/        部门管理 / 员工管理(含分配角色)
        ├── role/               角色管理(角色增删改 + 权限树授权)  ← 新增
        ├── report/emp/         员工信息统计(ECharts 柱状图 + 饼图)
        ├── report/stu/         学员信息统计(ECharts 柱状图 + 饼图)
        └── log/                日志管理
```

---

## 4. 与后端 RBAC 的配合

| 能力 | 实现位置 | 依赖接口 |
|---|---|---|
| 登录后存 token / 角色 / 权限 / 菜单 | `views/login/index.vue` | `POST /login` |
| 侧边栏菜单按权限动态渲染 | `views/layout/index.vue` | `GET /menus/mine` |
| 路由守卫（无权限的页面进不去） | `router/index.js` | 登录响应里的 `menus` |
| 按钮级显隐 `v-permission` | `utils/permission.js` | 登录响应里的 `permissions` |
| 角色管理 + 权限树授权 | `views/role/index.vue` | `GET/POST/PUT/DELETE /roles`、`GET /menus`、`PUT /roles/{id}/menus` |
| 给员工分配角色 | `views/emp/index.vue` | `GET/PUT /employees/{id}/roles` |

权限码与后端 `@RequiresPermission` 上的标识完全一致，例如：

```vue
<el-button v-permission="'class:create'">+ 新增班级</el-button>
<el-button v-permission="['employee:create', 'employee:update']">上传头像</el-button>
```

> ⚠️ 前端隐藏按钮 / 拦截路由都只是**体验优化**，不是安全边界。
> 绕过前端直接调接口，后端 `PermissionInterceptor` 一样会返回 403。

---

## 5. 已知说明

- 侧边栏菜单的**图标**按路由在前端维护（`views/layout/index.vue` 里的 `menuIcon`）：
  数据库 `menu` 表没有图标字段，新增页面时在前端补一行即可。
- 角色授权时提交的是「勾选的叶子节点 + 半选的父分组」：
  分组菜单本身也是 `menu` 表里的一行，不一起授权的话 `/menus/mine` 不会返回该分组，侧边栏会少一组。
- 上传走 `el-upload`，不经过 axios 拦截器，所以请求头里的 `token` 是手动带上的。
- 员工手机号前端用的是后端的规则 `^1\d{10}$`；学员手机号是 `^1[3-9]\d{9}$`，两者不同属历史遗留。
