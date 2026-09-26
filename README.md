# 培训机构后台管理系统

部门 / 员工 / 班级 / 学员四个业务模块的后台管理系统，含数据统计报表、RBAC 权限控制与操作日志审计。

前后端分离：前端是 Vue3 + Element Plus + ECharts 的**完整源码工程**（`vue-training-admin-backend/`），用 Vite 启动，
开发时由 Vite 把 `/api/` 反向代理到 Spring Boot，一个 `npm run dev` 就能跑起来。

- 接口文档：[docs/API.md](docs/API.md)（40 个接口的完整说明）
- 演示账号：`zhangwei / 123456`（超级管理员，含全部权限）

---

## 目录

- [功能模块](#功能模块)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [目录结构](#目录结构)
- [数据库设计](#数据库设计)
- [RBAC 权限控制](#rbac-权限控制)
- [操作日志](#操作日志)
- [文件存储](#文件存储)
- [前端工程与启动](#前端工程与启动)
- [配置说明](#配置说明)
- [常见问题](#常见问题)
- [已知待办](#已知待办)

---

## 功能模块

| 模块 | 功能 |
|---|---|
| 登录 | 用户名密码登录，下发 JWT 与权限、菜单 |
| 部门管理 | 增删改查；有员工时禁止删除 |
| 员工管理 | 增删改查、头像上传、工作经历（主从表）、按姓名/性别/入职日期筛选、分页；密码不返回、修改不回写 |
| 班级管理 | 增删改查、班主任指派、按结课时间筛选、状态按日期实时计算（未开班/在读中/已结课）；有学员时禁止删除 |
| 学员管理 | 增删改查、学号/身份证号唯一校验、违纪扣分累计、按姓名/学历/班级筛选 |
| 数据统计 | 员工职位分布、员工性别分布、各班级学员人数、学员学历分布（ECharts） |
| 操作日志 | 写操作自动留痕（操作人/时间/类名/方法/参数/结果/耗时/IP），超权行为也会记录 |
| 权限管理 | 角色增删改查、角色授权（权限树勾选）、员工分配角色 |
| 文件上传 | 头像等图片上传到本地磁盘，带类型/大小校验 |

页面路由：`/index` 首页、`/clazz` 班级、`/stu` 学员、`/dept` 部门、`/emp` 员工、`/report/emp` 员工统计、`/report/stu` 学员统计、`/log` 日志统计、`/role` 角色管理。

侧边栏菜单**不再是写死的**：登录后按 `GET /menus/mine` 下发的菜单动态渲染，按钮按权限码显隐（`v-permission`）。

---

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 4.0.6、MyBatis、PageHelper、JJWT 0.9.1 |
| 数据库 | MySQL 8+（库名 `training_admin`），InnoDB + 外键 + CHECK 约束 |
| 文件存储 | 本地磁盘（`LocalFileStorageService`） |
| 前端 | Vue3 + Vite + Element Plus + ECharts（完整源码工程 `vue-training-admin-backend/`） |
| 构建 / 部署 | Vite：`npm run dev` 开发、`npm run build` 出 `dist/` |
| 构建 | Maven |

---

## 快速开始

### 1. 前置条件

| 依赖 | 版本 | 验证命令 |
|---|---|---|
| JDK | 17+ | `java -version` |
| Maven | 3.6+ | `mvn -v` |
| MySQL | 8.0.16+（CHECK 约束需要 8.0.16 以上才真正生效） | `mysql --version` |
| Node.js | 16+（Vite 3 要求） | `node -v` |

### 2. 初始化数据库

```bash
cd training-admin-backend

mysql -uroot -p1234 --default-character-set=utf8mb4 < sql/create_schema.sql
mysql -uroot -p1234 --default-character-set=utf8mb4 < sql/inser_data.sql
```

- `create_schema.sql` 会先 `DROP DATABASE IF EXISTS training_admin` 再重建，**可重复执行**（会清空数据）。
- `inser_data.sql` 灌入 4 部门 / 9 员工 / 4 班级 / 10 学员 / 5 角色 / 31 项权限。
- 班级的开课结课时间用 `CURDATE()` 相对计算，所以导入后**三种班级状态都会出现**，其中「未开班」的班级 0 人（正好能验证报表的空班级统计）。

### 3. 启动后端

```bash
mvn spring-boot:run
# 或打包后运行
mvn clean package && java -jar target/training-admin-0.0.1-SNAPSHOT.jar
```

后端监听 **8080**，启动成功后可验证：

```bash
curl -X POST -H "Content-Type: application/json" \
     -d '{"username":"zhangwei","password":"123456"}' \
     http://localhost:8080/login
```

### 4. 启动前端（Vite）

```bash
cd ../vue-training-admin-backend
npm install          # 首次
npm run dev          # 启动开发服务器
```

浏览器会自动打开 **http://localhost:5173** （没自动打开就手动访问）。Vite 会把 `/api/**` 反向代理到 `http://localhost:8080/**`。

**在 VS Code 里启动（推荐）**：用 VS Code 打开仓库根目录，按 `F5`，选择

| 配置 | 作用 |
|---|---|
| 前端：dev 启动 vite | 跑 vite 并自动打开 `http://localhost:5173/`，**平时点这个** |
| 前端：Chrome 调试（需先启动 dev） | 需要打断点时：先启动上一个，再启动这个 |

配置在 `.vscode/launch.json` 与 `.vscode/tasks.json`；任务面板里还有 `前端: dev`（推荐，直接起 node、不走 shell）、`前端: npm run dev`、`前端: build`、`后端: mvn spring-boot:run`。

> **报 `意外的标记"-Command"` 时**：这是 VS Code 用户级 `settings.json` 里 PowerShell 终端 profile 的
> `args` 用了 `-Command`，与 VS Code 跑任务时自己拼的 `-Command` 撞车导致的，与本项目无关。
> 本项目已用 `.vscode/settings.json` 的 `terminal.integrated.automationProfile.windows` 绕开，
> 并用 `type: "process"` 的 `前端: dev` 任务彻底不经过 shell；彻底修法见
> [前端工程与启动](#前端工程与启动) 与 `vue-training-admin-backend/README.md`。

> 只启前端不启后端时，页面能打开但登录会失败，前端会提示「接口访问异常, 请确认后端服务已启动」。

---

## 目录结构

```
training-admin-backend/
├── pom.xml
├── README.md                      本文件
├── docs/
│   └── API.md                     接口文档(40 个接口)
├── sql/
│   ├── create_schema.sql              建库建表: 业务表 + RBAC 表(外键/索引/CHECK)
│   └── inser_data.sql           初始化数据(含角色与权限分配)
├── uploads/                       上传文件存储目录
│   └── avatar/photo.jpg           种子数据里所有员工头像使用的图片
└── src/
    ├── main/java/com/qiming/
    │   ├── TrainingAdminApplication.java
    │   ├── annotation/
    │   │   └── RequiresPermission.java       接口权限注解
    │   ├── config/
    │   │   ├── WebConfig.java                拦截器注册 + 静态资源映射
    │   │   └── FileStorageProperties.java    文件存储配置
    │   ├── controller/                       10 个控制器
    │   ├── service/ + service/impl/          业务接口与实现
    │   ├── mapper/                           MyBatis Mapper 接口
    │   ├── pojo/                             实体、查询参数、表单、返回结构
    │   ├── filter/
    │   │   └── OperationLogFilter.java       缓存请求/响应体供日志使用
    │   ├── interceptor/
    │   │   ├── TokenInterceptor.java         登录鉴权 + 写入当前用户上下文
    │   │   ├── OperationLogInterceptor.java  操作日志
    │   │   └── PermissionInterceptor.java    RBAC 权限校验
    │   ├── exception/
    │   │   ├── GlobalExceptionHandler.java   全局异常处理
    │   │   └── ForbiddenException.java       权限不足异常
    │   └── utils/
    │       ├── JwtUtils.java                 JWT 生成与解析
    │       ├── PageUtils.java                分页兜底与上限保护
    │       └── UserContext.java              当前登录人(ThreadLocal)
    ├── main/resources/
    │   ├── application.yml
    │   ├── logback.xml
    │   └── com/qiming/mapper/*.xml           6 个 Mapper XML
    └── test/java/com/qiming/                 默认的上下文加载测试
```

前端工程（Vue3 + Vite + Element Plus + ECharts，可独立启动）：

```
vue-training-admin-backend/
├── index.html
├── vite.config.js              别名 @ -> src, dev 端口 5173, /api -> 8080 代理
├── package.json                npm run dev / build / preview
├── .vscode/                    launch.json + tasks.json (VS Code 按 F5 启动)
└── src/
    ├── main.js                 注册 Element Plus(中文)、全部图标、v-permission 指令
    ├── utils/
    │   ├── request.js          axios: 注入 token, 统一处理 401 / 403
    │   └── permission.js       按钮级权限指令 v-permission
    ├── api/                    12 个接口模块, 与后端 Controller 一一对应
    ├── router/index.js         路由表 + 菜单权限守卫
    └── views/
        ├── layout/             顶部栏 + 按 /menus/mine 动态渲染的侧边栏
        ├── login/  index/      登录页 / 首页
        ├── clazz/  stu/        班级管理 / 学员管理
        ├── dept/   emp/        部门管理 / 员工管理(含分配角色)
        ├── role/               角色管理(角色增删改 + 权限树授权)
        ├── report/emp/         员工信息统计
        ├── report/stu/         学员信息统计
        └── log/                日志管理
```

---

## 数据库设计

### 业务表

| 表 | 说明 | 关键约束 |
|---|---|---|
| `department` | 部门 | `name` 唯一 |
| `employee` | 员工 | `username`/`phone` 唯一；`department_id → department.id` |
| `employee_experience` | 员工工作经历 | `employee_id → employee.id`（**ON DELETE CASCADE**） |
| `course_class` | 班级 | `name` 唯一；`head_teacher_id → employee.id`；**`CHECK (end_date > begin_date)`** |
| `student` | 学员 | `student_no`/`phone`/`id_card` 唯一；`class_id → course_class.id` |
| `operation_log` | 操作日志 | 刻意不建外键——员工删除后日志仍需保留 |

### RBAC 表

| 表 | 说明 | 关键约束 |
|---|---|---|
| `role` | 角色 | `name`/`code` 唯一 |
| `menu` | 菜单 / 权限（`permission_code` 为权限标识） | `permission_code` 唯一 |
| `employee_role` | 员工-角色关联 | 双向级联删除 |
| `role_menu` | 角色-菜单关联 | 双向级联删除 |

```
部门 department ──< 员工 employee ──< 工作经历 employee_experience
                        │
                        ├──< 员工角色 >── 角色 role ──< 角色菜单 >── 菜单/权限 menu
                        │
                  班主任 │
                        ▼
                     班级 course_class ──< 学员 student
```

### 设计要点

- **外键 + CHECK 兜底**：删除部门/班级/员工/角色时的业务约束在服务层给出友好提示，
  数据库外键再兜一层，避免脏数据绕过服务层写入。
- **班级状态不入库**：由 `begin_date` / `end_date` 与当天比较实时算出，
  不需要定时任务；代价是无法在数据库层按状态筛选。
- **密码列 `NOT NULL DEFAULT '123456'`**：从库层面杜绝"新增员工密码为 NULL 导致无法登录"。
- **日志表不建外键**：员工删除后历史操作记录必须留存。

---

## RBAC 权限控制

### 模型

`员工 ──< employee_role >── 角色 ──< role_menu >── 菜单/权限`

`menu` 一张表承载两类数据，都用 `permission_code` 作为权限标识：

| `type` | 含义 | 说明 |
|---|---|---|
| 1 | 菜单 | 有 `path`，用于前端路由与可见菜单 |
| 2 | 按钮 / 接口权限 | 如 `class:delete`，用于后端鉴权 |

分组菜单（如「班级学员管理」）不设权限码，只做前端分组。

### 权限码

| 模块 | 权限码 |
|---|---|
| 班级 | `class:list` `class:create` `class:update` `class:delete` |
| 学员 | `student:list` `student:create` `student:update` `student:delete` `student:violation` |
| 部门 | `department:list` `department:create` `department:update` `department:delete` |
| 员工 | `employee:list` `employee:create` `employee:update` `employee:delete` `employee:assign-role` |
| 报表 | `report:employee` `report:student` `report:log` |
| 角色 | `role:list` `role:create` `role:update` `role:delete` `role:assign-menu` |

完整的角色-权限矩阵见 [docs/API.md 第 2 节](docs/API.md#2-权限模型)。

### 校验方式

在 Controller 方法上标注 `@RequiresPermission`，由 `PermissionInterceptor` 在进入方法前校验：

```java
@DeleteMapping
@RequiresPermission("employee:delete")                        // 单个权限
public Result deleteEmployee(@RequestParam List<Integer> ids) { ... }

@PostMapping("/upload")
@RequiresPermission({"employee:create", "employee:update"})   // 满足其一即可
public Result upload(...) { ... }
```

- 拥有 `ADMIN` 角色的用户**兜底放行全部接口**，新增权限时无需回头补授权。
- 权限不足抛 `ForbiddenException` → 全局异常处理返回 **HTTP 403** +
  `{"code":0,"msg":"权限不足: 需要【employee:delete】权限, 请联系管理员!"}`。
- 拦截器顺序 `Token → 操作日志 → 权限`：401 不记日志，**403 会留痕**，便于审计谁在试图越权。
- 当前登录人由 `TokenInterceptor` 写入 `UserContext`（ThreadLocal，请求结束清理，避免线程复用串号）。

> **参考数据接口例外**：`GET /employees/list`、`GET /course-classes/list`、`GET /departments`
> 是各表单下拉框依赖的数据，标为「满足相关模块任一权限即可」或对已登录用户开放，
> 避免出现「有学员管理权限却选不了班级」这类跨模块死锁。
> 其中 `/employees/list` 只返回 `id` 与 `name`，不暴露手机号与薪资。

### 内置角色与演示账号

| 角色 | 标识 | 权限范围 | 演示账号 |
|---|---|---|---|
| 超级管理员 | `ADMIN` | 全部（31 项） | `zhangwei` |
| 教研主管 | `TEACHING_DIRECTOR` | 首页 + 班级/学员查看 + 全部报表（含日志） | — |
| 班主任 | `HEAD_TEACHER` | 班级增改查 + 学员增改/违纪 + 报表 | `liuyang` / `chenyue` / `zhaomin` |
| 咨询师 | `CONSULTANT` | 学员新增/修改/查看 + 报表 | `sunli` / `zhouhao` |
| 讲师 | `LECTURER` | 只读（班级/学员查看 + 报表） | `lina` / `wangqiang` / `wujing` |

密码统一为 `123456`。切换账号登录即可直观看到接口权限与可访问页面的差异。

### 权限管理怎么用

前端已有**角色管理页面**（路由 `/role`，侧边栏「权限管理 → 角色管理」）：
可以增删改角色，并用权限树勾选该角色能访问的菜单、能执行的操作；保存后立即生效，员工无需重新登录。
给员工分配角色则在「员工管理」页的「分配角色」按钮里。

接口方式（curl）同样可用：

```bash
TOKEN=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{"username":"zhangwei","password":"123456"}' \
  http://localhost:8080/login | sed -E 's/.*"token":"([^"]+)".*/\1/')

curl -H "token: $TOKEN" http://localhost:8080/roles          # 角色列表
curl -H "token: $TOKEN" http://localhost:8080/menus          # 全部权限树(授权界面用)
curl -H "token: $TOKEN" http://localhost:8080/roles/3/menus  # 角色已勾选的权限ID
curl -X PUT -H "token: $TOKEN" -H "Content-Type: application/json" \
     -d "[1,2,3,4,5,7,8,9,11,22,23,24]" http://localhost:8080/roles/3/menus
curl -X PUT -H "token: $TOKEN" -H "Content-Type: application/json" \
     -d "[3]" http://localhost:8080/employees/5/roles
```

`ADMIN` 角色不允许改标识、不允许删除；已分配给员工的角色不允许直接删除。
更多示例见 [docs/API.md 第 11.3 节](docs/API.md#113-示例新建一个只读审计角色)。

---

## 操作日志

写操作（POST / PUT / DELETE / PATCH）自动记录，查询接口不记。

```json
{
  "operatorName": "李娜",
  "className": "CourseClassController",
  "methodName": "addCourseClass",
  "methodParams": "{\"name\":\"x\"}",
  "returnValue": "失败: 权限不足: 需要【class:create】权限, 请联系管理员!",
  "costTime": 6,
  "operationIp": "0:0:0:0:0:0:0:1"
}
```

| 规则 | 说明 |
|---|---|
| 只记写操作 | 否则每翻一页就插一行，日志表很快失去意义 |
| 不记完整响应体 | 只记 `成功` / `失败: 原因`。**登录响应体里带 token**，整段落库等于把 12 小时有效的令牌泄漏给所有能看日志页的人 |
| `password` 掩码 | 请求参数中的密码替换为 `****` |
| 401 不记、403 记 | 未登录不产生日志；越权被拒留痕 |
| 独立事务 | `REQUIRES_NEW`，业务回滚也照样留痕，且写日志失败不影响接口响应 |
| 实现方式 | `OperationLogFilter`（缓存请求/响应）+ `OperationLogInterceptor`（取类名方法名耗时） |

> 未使用 AOP：本地仓库缺少 Spring Boot 4 对应的 `spring-boot-starter-aop`，
> 只有 2019 年的 AspectJ 1.9.4，装进 Boot 4 有兼容风险，故改用过滤器 + 拦截器实现。

---

## 文件存储

- 上传接口 `POST /files/upload`，落地到 `{file.storage.local-dir}/{yyyy}/{MM}/{uuid}.{ext}`
- 校验：扩展名白名单 + Content-Type 必须 `image/*` + 大小 ≤ 10MB
- 文件名用 UUID，落盘前做路径归一化校验，防路径穿越
- 对外通过 `GET /uploads/**` 静态访问，**该路径免鉴权**（`<img>` 标签无法携带请求头）
- 返回给前端的 URL 带 `access-prefix`（默认 `/api/uploads`），与前端统一的 `/api` 前缀保持一致，由 Vite 代理转发到后端

```bash
curl -X POST -H "token: $TOKEN" -F "file=@photo.jpg;type=image/jpeg" \
     http://localhost:8080/files/upload
# => {"code":1,"data":"/api/uploads/2026/09/xxx.jpg","msg":"success"}
```

原本使用阿里云 OSS，已改为本地存储：不再依赖外部云服务与访问凭证，
依赖树也从 99 个 jar 降到 76 个。

---

## 前端工程与启动

前端源码在 `vue-training-admin-backend/`（Vue 3 + Vite + Element Plus + ECharts），
用 `npm run dev` 或 VS Code 的 `F5` 启动。

| 用途 | 地址 |
|---|---|
| 页面 | http://localhost:5173/ |
| 接口 | http://localhost:5173/api/** → Vite 代理到 http://localhost:8080/** |
| 头像等上传文件 | http://localhost:5173/api/uploads/avatar/photo.jpg |

所有请求都以 `/api` 开头，由 `vite.config.js` 里的 `server.proxy` 去掉前缀后转发给后端，
所以后端 `file.storage.access-prefix` 保持默认的 `/api/uploads` 即可，两边都不需要额外配置。

### 改前端时的注意事项

改源码（`vue-training-admin-backend/src/`）保存后 Vite 会热更新，通常不用刷新。
改完跑一次 `npm run build` 确认能打包通过即可。

> **不要在 PowerShell 5.1 里用不带 `-Encoding UTF8` 的 `Get-Content` 读写前端文件**。
> 源码是无 BOM 的 UTF-8，默认按 GBK 解码再写回会造成中文二次编码（标题、校验提示变乱码）。
> 读取请用 `[System.IO.File]::ReadAllText($p, [System.Text.Encoding]::UTF8)`。
> （同类坑也会发生在 `.ps1` 脚本上：5.1 读无 BOM 的脚本按 ANSI 解码，脚本里别写非 ASCII 更稳妥。）

### 前端已有的行为

- axios 请求拦截器自动带上 `localStorage` 里的 `token`
- 响应拦截器：`401` 提示"登录失效"并跳登录页；`403` 直接弹出后端返回的 `msg`
- 路由守卫：未登录跳登录页；已登录时按登录接口下发的 `menus` 判断能否进入某页面，
  无权限则提示并回首页
- 菜单来自 `GET /menus/mine`，**按权限动态渲染**：受限用户看不到无权菜单，直接输 URL 也会被路由守卫拦下
- 按钮按权限码显隐（全局指令 `v-permission`），例如没有 `class:create` 权限就看不到「新增班级」
- **表格「操作」列在没有任何操作权限时整列隐藏**（列级 `v-if="hasPermission([...])"`），
  避免只读角色（如讲师）看到一列带边框的空白；只有部分权限时列保留、按钮各自显隐

> **前端这些判断不是安全边界**，只是体验优化。绕过前端直接调接口，服务端一样会拦。

---

## 配置说明

### application.yml

```yaml
spring:
  application:
    name: training-admin
  datasource:
    url: jdbc:mysql://localhost:3306/training_admin?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: 1234
  servlet:
    multipart:
      max-file-size: 10MB      # 与前端"只能上传10M以内图片"一致
      max-request-size: 20MB

mybatis:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl   # 会打印全部 SQL, 生产应关闭
    map-underscore-to-camel-case: true

file:
  storage:
    local-dir: ./uploads        # 存储根目录, 相对应用工作目录
    access-prefix: /api/uploads # 对外访问前缀; 前端直连后端时改成 /uploads
    max-size: 10485760          # 单文件上限 10MB
```

### 需要注意的配置项

| 配置 | 说明 |
|---|---|
| `spring.datasource.password` | 明文写在配置里，生产应改为环境变量 |
| `file.storage.local-dir` | **相对路径以应用工作目录为基准**，换启动目录会导致上传文件"消失" |
| `file.storage.access-prefix` | 上传文件的对外 URL 前缀。默认 `/api/uploads` 配合前端的 `/api` 代理；若浏览器直连后端则改成 `/uploads` |
| `mybatis...log-impl` | `StdOutImpl` 会把登录 SQL 的参数（含密码）打进控制台，生产应关闭 |

### 分页上限

`PageUtils.MAX_PAGE_SIZE = 100`。前端所有 `page-sizes` 选项（最大 100）都在这个范围内。
`pageSize=1000000` 会被截断为 100，避免客户端一次拉全表。

---

## 常见问题

### VS Code 里点「运行」报 `表达式或语句中包含意外的标记"-Command"`

这是 VS Code **用户级** `settings.json` 里 PowerShell 终端 profile 的 `args` 写成了
`["-NoExit", "-Command", "<UTF-8设置>"]`：VS Code 跑任务/脚本时会**再拼一条自己的 `-Command`**，
两条撞在一起，第二个 `-Command` 就成了非法标记。与本项目代码无关。

本项目已用 `.vscode/settings.json` 的 `terminal.integrated.automationProfile.windows` 绕开，
并把 `前端: dev` 任务改成 `type: "process"`（直接起 node，不经过任何 shell）。
彻底修法见 [前端工程与启动](#前端工程与启动)。

### 页面能打开，但所有表格都是空的

按顺序排查：

1. **后端没启动** —— 最常见。`netstat -ano | findstr :8080` 没有监听就启动后端。
   后端不可用时前端会提示「接口访问异常, 请确认后端服务已启动」。
2. **浏览器缓存了旧前端包** —— 按 `Ctrl + F5` 强制刷新。
3. **登录态失效** —— 清掉 `localStorage` 里的 `loginUser` 重新登录。
   注意种子数据里的账号是 `zhangwei` / `liuyang` 等，用旧账号会提示"用户名不存在"。
4. **权限不足** —— 换个有权限的账号，或看接口返回的 `msg` 里缺哪个权限。
   403 时前端会把后端的 `msg` 直接弹出来，例如「权限不足: 需要【class:create】权限, 请联系管理员!」。

### 登录后侧边栏菜单没变化

菜单现在按 `GET /menus/mine` 动态渲染，会随权限变化。
若改了角色但菜单没动，强制刷新一次页面（菜单在进入主布局时拉取）。
直接输入无权页面的 URL 会提示"没有访问该页面的权限!"并回到首页。

### 头像不显示

- 检查 `uploads/avatar/photo.jpg` 是否存在（种子数据依赖它）。
- 检查 `access-prefix` 与实际访问方式是否匹配（默认 `/api/uploads`，浏览器直连后端则是 `/uploads`）。
- 该接口免鉴权，若返回 401，说明 `/uploads/**` 没被加入拦截器白名单。

### 新增员工后无法登录

密码列是 `NOT NULL DEFAULT '123456'`，新增不传密码时服务端会补 `123456`。
如果仍然登录失败，检查是不是改了 `password` 列的默认值或服务端默认值。

### 修改班级时报"结课时间必须晚于开课时间"

服务层与数据库 CHECK 双重校验。请检查弹窗里的开课/结课时间是否填反。

### 源码中文注释变成乱码

用 PowerShell 5.1 的 `Get-Content`（不带 `-Encoding UTF8`）读无 BOM 的 UTF-8 文件会按 GBK 解码，
写回后就是二次编码。统一改用：

```powershell
[System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
[System.IO.File]::WriteAllText($path, $text, (New-Object System.Text.UTF8Encoding($false)))
```

### 数据库连不上

确认 MySQL 已启动、库名是 `training_admin`、账号密码与 `application.yml` 一致。
若 `create_schema.sql` 执行报 CHECK 约束相关的错，说明 MySQL 低于 8.0.16，请升级。

---

## 已知待办

### 安全

- [ ] 密码为**明文比对**，应改为 BCrypt 加盐哈希，并新增「修改密码」接口（含原密码校验）
- [ ] JWT 密钥硬编码在 `JwtUtils`，应移到配置或环境变量；令牌无刷新机制、无服务端吊销
- [ ] `mybatis log-impl: StdOutImpl` 会打印登录 SQL 参数（含密码），生产必须关闭
- [ ] 操作日志中的 `id_card`、`phone` 等字段仍是明文入库，建议加字段级掩码
- [ ] 生产环境应启用 HTTPS，并加上 CSP / X-Frame-Options 等安全响应头

### 功能

- [ ] 学员/员工缺少 Excel 批量导入导出
- [ ] 班级状态无法按状态筛选（状态由日期实时计算，未入库）
- [ ] 学员违纪只累计次数与总分，缺少明细表，无法查看/撤销历史违纪
- [ ] 缺少缴费、课程考勤、线索跟进等业务模块
- [ ] 无个人中心（修改密码、更换头像）

### 工程

- [ ] 除默认的上下文加载测试外**没有单元测试**
- [ ] 接口文档为手写 Markdown；可引入 springdoc-openapi（Boot 4 需对应版本）自动生成 Swagger UI
- [ ] 枚举值（性别/职位/学历/学科）硬编码在 SQL、Java、前端三处，建议改为数据字典表
- [ ] 分页参数、删除接口参数风格不统一（详见 [docs/API.md 13.4](docs/API.md#134-接口设计上的已知不一致)）
- [ ] 权限校验每次请求查 2 次库，可按员工ID加缓存、在角色变更时失效
- [ ] 无多环境配置（dev/prod profile），数据库密码明文写在版本库中
