# 培训机构后台管理系统 · 接口文档

| 项 | 值 |
|---|---|
| 后端 | Spring Boot 4.0.6 + MyBatis + MySQL 8 |
| 后端地址 | `http://localhost:8080` |
| 经 nginx 访问 | `http://localhost:90/api`（nginx 把 `/api/` 反向代理到后端根路径） |
| 鉴权方式 | 请求头 `token`，值为 `POST /login` 返回的 JWT（有效期 12 小时） |
| 字符编码 | 统一 UTF-8 |
| 接口总数 | 32 个 |

---

## 1. 通用约定

### 1.1 统一响应结构

所有接口（含异常）都返回同一结构：

```json
{
  "code": 1,
  "msg": "success",
  "data": { }
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | int | **1 = 成功，0 = 失败**（不是 HTTP 状态码） |
| `msg` | string | 成功固定为 `success`；失败时为可读的中文原因 |
| `data` | any | 业务数据；失败时为 `null`，无返回值的成功接口也为 `null` |

### 1.2 HTTP 状态码

| 状态码 | 场景 | 响应体 |
|---|---|---|
| `200` | 成功，或**业务失败**（如"密码错误!"、"该班级下有学生"） | 统一结构，`code=0` |
| `401` | 请求头没有 `token`，或令牌无效/过期 | **无响应体**，前端据此跳登录页 |
| `403` | 已登录但**权限不足** | 统一结构，`code=0`，`msg` 说明缺少哪个权限 |
| `500` | 未预期的服务端异常 | 统一结构，`msg` 为 `程序出错了!` |

> ⚠️ 这是本项目的历史约定：业务失败用 `200 + code=0`，不用 4xx。
> 前端已经按这个约定处理，改动会连带影响前端，故未调整。
> 只有鉴权（401）与鉴权（403）使用了真实状态码。

### 1.3 鉴权

除下列接口外，**所有接口**都必须携带请求头 `token`：

| 免鉴权接口 | 原因 |
|---|---|
| `POST /login` | 登录本身 |
| `GET /uploads/**` | 头像等静态文件，由 `<img>` 标签直接请求，无法携带请求头 |

```bash
curl -H "token: eyJhbGciOiJIUzI1NiJ9..." http://localhost:8080/employees
```

当前登录人由 `TokenInterceptor` 校验后写入 `UserContext`，权限由 `PermissionInterceptor` 校验
（RBAC 模型见 [第 2 节](#2-权限模型) 与 README）。

### 1.4 分页约定

| 参数 | 类型 | 默认 | 上限 | 说明 |
|---|---|---|---|---|
| `page` | int | `1` | — | 小于 1 按 1 处理 |
| `pageSize` | int | `10` | **100** | 小于 1 按 10 处理；超过 100 截断为 100 |

分页响应统一为 `PageResult`：

```json
{ "total": 9, "rows": [ { }, { } ] }
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `total` | long | 满足条件的总记录数 |
| `rows` | array | 当前页数据 |

未显式说明时，列表排序为 `update_time DESC, id DESC`（加 `id` 兜底保证翻页不重不漏）。

### 1.5 时间与命名

- 请求/响应中的日期：`yyyy-MM-dd`（如 `2024-03-01`）
- 请求/响应中的日期时间：ISO 格式 `yyyy-MM-ddTHH:mm:ss`（如 `2026-09-25T13:22:50`）
- JSON 字段统一小驼峰；数据库列名下划线，由 `map-underscore-to-camel-case` 自动映射

### 1.6 参数校验

校验在**服务端**执行（前端表单校验只是体验，可被绕过）。校验不通过返回 `code=0` 与可读原因。
各模块的具体规则见对应章节。

---

## 2. 权限模型

模型：`员工 employee ──< employee_role >── 角色 role ──< role_menu >── 菜单/权限 menu`

权限标识形如 `模块:操作`。拥有 `ADMIN` 角色的用户放行全部接口。
标注为「满足其一」的接口，命中任意一个权限即可通过。

### 2.1 权限码清单

| 模块 | 权限码 | 说明 |
|---|---|---|
| 班级 | `class:list` / `class:create` / `class:update` / `class:delete` | 查询/新增/修改/删除 |
| 学员 | `student:list` / `student:create` / `student:update` / `student:delete` / `student:violation` | 违纪处理单独授权 |
| 部门 | `department:list` / `department:create` / `department:update` / `department:delete` | |
| 员工 | `employee:list` / `employee:create` / `employee:update` / `employee:delete` / `employee:assign-role` | |
| 报表 | `report:employee` / `report:student` / `report:log` | 员工统计/学员统计/日志统计 |
| 角色 | `role:list` / `role:create` / `role:update` / `role:delete` / `role:assign-menu` | |
| 首页 | `menu:index` | 仅用于菜单可见性，不对应接口 |

### 2.2 各角色权限矩阵

| 权限码 | 超级管理员<br>ADMIN | 教研主管<br>TEACHING_DIRECTOR | 班主任<br>HEAD_TEACHER | 咨询师<br>CONSULTANT | 讲师<br>LECTURER |
|---|:--:|:--:|:--:|:--:|:--:|
| `menu:index` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `class:list` | ✅ | ✅ | ✅ | — | ✅ |
| `class:create` | ✅ | — | ✅ | — | — |
| `class:update` | ✅ | — | ✅ | — | — |
| `class:delete` | ✅ | — | — | — | — |
| `student:list` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `student:create` | ✅ | — | ✅ | ✅ | — |
| `student:update` | ✅ | — | ✅ | ✅ | — |
| `student:delete` | ✅ | — | — | — | — |
| `student:violation` | ✅ | — | ✅ | — | — |
| `department:*` | ✅ | — | — | — | — |
| `employee:*` | ✅ | — | — | — | — |
| `report:employee` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `report:student` | ✅ | ✅ | ✅ | ✅ | ✅ |
| `report:log` | ✅ | ✅ | — | — | — |
| `role:*` | ✅ | — | — | — | — |

> 超级管理员在种子数据里有全部 31 项权限，**并且在代码里做兜底放行**，
> 因此以后新增权限无需再回头给 ADMIN 补授权。

### 2.3 演示账号

| 账号 | 姓名 | 角色 | 密码 |
|---|---|---|---|
| `zhangwei` | 张伟 | 超级管理员 | `123456` |
| `lina` / `wangqiang` | 李娜 / 王强 | 讲师（只读） | `123456` |
| `chenyue` / `liuyang` / `zhaomin` | 陈玥 / 刘洋 / 赵敏 | 班主任 | `123456` |
| `sunli` / `zhouhao` | 孙丽 / 周浩 | 咨询师 | `123456` |
| `wujing` | 吴静 | 讲师（只读） | `123456` |

---

## 3. 接口总览

`权限` 列为 `—` 表示只需登录，`免鉴权` 表示连 token 都不需要。

| # | 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|---|
| 1 | POST | `/login` | 登录 | 免鉴权 |
| 2 | GET | `/employees` | 分页查询员工 | `employee:list` |
| 3 | GET | `/employees/{id}` | 查询员工详情（含工作经历） | `employee:list` |
| 4 | POST | `/employees` | 新增员工 | `employee:create` |
| 5 | PUT | `/employees` | 修改员工 | `employee:update` |
| 6 | DELETE | `/employees?ids=` | 批量删除员工 | `employee:delete` |
| 7 | GET | `/employees/list` | 员工下拉框（仅 id+name） | — |
| 8 | GET | `/employees/{id}/roles` | 查询员工拥有的角色ID | `employee:list` |
| 9 | PUT | `/employees/{id}/roles` | 为员工分配角色 | `employee:assign-role` |
| 10 | GET | `/departments` | 查询全部部门 | `department:list` 或 `employee:list` |
| 11 | GET | `/departments/{id}` | 查询单个部门 | `department:list` |
| 12 | POST | `/departments` | 新增部门 | `department:create` |
| 13 | PUT | `/departments` | 修改部门 | `department:update` |
| 14 | DELETE | `/departments?id=` | 删除部门 | `department:delete` |
| 15 | GET | `/course-classes` | 分页查询班级 | `class:list` |
| 16 | GET | `/course-classes/{id}` | 查询单个班级 | `class:list` |
| 17 | POST | `/course-classes` | 新增班级 | `class:create` |
| 18 | PUT | `/course-classes` | 修改班级 | `class:update` |
| 19 | DELETE | `/course-classes/{id}` | 删除班级 | `class:delete` |
| 20 | GET | `/course-classes/list` | 班级下拉框 | `class:list`/`student:list`/`student:create`/`student:update` |
| 21 | GET | `/students` | 分页查询学员 | `student:list` |
| 22 | GET | `/students/{id}` | 查询学员详情 | `student:list` |
| 23 | POST | `/students` | 新增学员 | `student:create` |
| 24 | PUT | `/students` | 修改学员 | `student:update` |
| 25 | DELETE | `/students/{ids}` | 批量删除学员 | `student:delete` |
| 26 | PUT | `/students/violations/{id}/{score}` | 学员违纪扣分 | `student:violation` |
| 27 | GET | `/reports/employee-positions` | 员工职位分布 | `report:employee` |
| 28 | GET | `/reports/employee-genders` | 员工性别分布 | `report:employee` |
| 29 | GET | `/reports/course-class-student-counts` | 各班级学员人数 | `report:student` |
| 30 | GET | `/reports/student-degrees` | 学员学历分布 | `report:student` |
| 31 | GET | `/operation-logs/page` | 分页查询操作日志 | `report:log` |
| 32 | GET | `/roles` | 查询全部角色 | `role:list` |
| 33 | GET | `/roles/{id}/menus` | 查询角色拥有的权限ID | `role:list` |
| 34 | POST | `/roles` | 新增角色 | `role:create` |
| 35 | PUT | `/roles` | 修改角色 | `role:update` |
| 36 | DELETE | `/roles/{id}` | 删除角色 | `role:delete` |
| 37 | PUT | `/roles/{id}/menus` | 为角色分配权限 | `role:assign-menu` |
| 38 | GET | `/menus` | 全部菜单权限树 | `role:list` |
| 39 | GET | `/menus/mine` | 当前用户可见菜单 | — |
| 40 | POST | `/files/upload` | 上传文件 | `employee:create` 或 `employee:update` |

> 表中编号按模块分组连续；实际接口共 40 个方法映射（含下拉框等辅助接口）。

---

## 4. 登录

### 4.1 登录

`POST /login` ｜ 权限：**免鉴权**

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `username` | string | 是 | 用户名 |
| `password` | string | 是 | 密码（当前为明文比对） |

```json
{ "username": "zhangwei", "password": "123456" }
```

**响应 `data`（LoginInfo）**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 员工ID |
| `username` | string | 用户名 |
| `name` | string | 姓名 |
| `token` | string | JWT，后续请求放在请求头 `token` 中 |
| `roles` | string[] | 角色标识，如 `["ADMIN"]` |
| `permissions` | string[] | 权限标识集合 |
| `menus` | Menu[] | 可见菜单（仅 `type=1`，带 `path`），供前端路由守卫使用 |

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "zhangwei",
    "name": "张伟",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "roles": ["ADMIN"],
    "permissions": ["class:create", "class:delete", "class:list", "..."],
    "menus": [
      { "id": 1, "parentId": 0, "name": "首页", "type": 1, "permissionCode": "menu:index", "path": "/index", "sort": 1 },
      { "id": 2, "parentId": 0, "name": "班级学员管理", "type": 1, "permissionCode": null, "path": null, "sort": 2 }
    ]
  }
}
```

**可能的失败**

| `msg` | 触发条件 |
|---|---|
| `用户名不能为空!` | 未传 `username` |
| `用户名不存在!` | 用户名不存在 |
| `密码错误!` | 密码不匹配（密码为空的账号也走这里） |

> 登录成功与失败**都会写入操作日志**（失败时 `operator_name` 取请求体里的 `username`），
> 便于排查暴力破解。日志中的 `password` 会被掩码为 `****`。

---

## 5. 员工管理

### 5.1 字段字典

**Employee（员工）**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 主键 |
| `username` | string | 登录用户名，唯一，2~20 字符 |
| `password` | string | 登录密码。**只写不读**：可提交，但任何响应都不会返回它 |
| `name` | string | 姓名，≤10 字符 |
| `gender` | int | 性别，1:男 2:女 |
| `phone` | string | 手机号，`^1\d{10}$`，唯一 |
| `position` | int | 职位，见 [12.1](#121-枚举字典) |
| `salary` | int | 薪资 |
| `avatar` | string | 头像访问地址，如 `/api/uploads/avatar/photo.jpg` |
| `entryDate` | date | 入职日期 |
| `departmentId` | int | 所属部门ID |
| `departmentName` | string | 所属部门名称（关联查询结果，只读） |
| `createTime` / `updateTime` | datetime | 创建/修改时间 |
| `experienceList` | EmployeeExperience[] | 工作经历列表 |

**EmployeeExperience（工作经历）**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 主键（新增时无需传） |
| `employeeId` | int | 员工ID（由服务端回填，无需传） |
| `beginDate` | date | 开始时间 |
| `endDate` | date | 结束时间 |
| `company` | string | 公司名称 |
| `position` | string | 职位（字符串，与员工的 `position` 数字枚举不同） |

### 5.2 分页查询员工

`GET /employees` ｜ 权限：`employee:list`

**Query 参数**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `name` | string | 否 | 姓名模糊匹配 |
| `gender` | int | 否 | 1 或 2 |
| `begin` | date | 否 | **入职日期**区间开始（含当天） |
| `end` | date | 否 | **入职日期**区间结束（含当天） |
| `page` / `pageSize` | int | 否 | 分页，默认 1 / 10，上限 100 |

> `begin` / `end` 必须同时传才生效。筛选的是 `entry_date`（入职日期），不是创建时间。

```bash
curl -H "token: $TOKEN" \
  "http://localhost:8080/employees?name=张&gender=1&begin=2021-01-01&end=2024-12-31&page=1&pageSize=10"
```

**响应 `data`**：`PageResult<Employee>`（`rows` 中不含 `password`，`experienceList` 为 `null`）

### 5.3 查询员工详情

`GET /employees/{id}` ｜ 权限：`employee:list`

返回完整的 `Employee`，**包含 `experienceList`**，不包含 `password`。
员工不存在时 `data` 为 `null`、`code=1`。

### 5.4 新增员工

`POST /employees` ｜ 权限：`employee:create`

**请求体**：`Employee`（`password` 可不传，服务端补默认值 `123456`）

```json
{
  "username": "newuser",
  "name": "新员工",
  "gender": 1,
  "phone": "13800000010",
  "position": 2,
  "salary": 15000,
  "avatar": "/api/uploads/avatar/photo.jpg",
  "entryDate": "2025-01-06",
  "departmentId": 1,
  "experienceList": [
    { "beginDate": "2020-01-01", "endDate": "2022-12-31", "company": "某公司", "position": "开发工程师" }
  ]
}
```

**校验规则**

| 字段 | 规则 | 失败提示 |
|---|---|---|
| `username` | 非空、2~20 字符、唯一 | `用户名不能为空!` / `用户名长度应在2到20个字符之间!` / `newuser已存在!` |
| `name` | 非空、≤10 字符 | `姓名不能为空!` / `姓名长度不能超过10个字符!` |
| `gender` | 必填，1 或 2 | `请选择正确的性别!` |
| `phone` | 非空、`^1\d{10}$`、唯一 | `手机号不能为空!` / `请输入有效的手机号!` |
| `departmentId` | 若传则必须存在 | `所选部门不存在, 请重新选择!` |

**响应 `data`**：`null`

> 工作经历与员工在**同一事务**中写入，任一失败整体回滚。

### 5.5 修改员工

`PUT /employees` ｜ 权限：`employee:update`

请求体同新增，但**必须带 `id`**。

| 注意 | 说明 |
|---|---|
| **不修改密码** | SQL 中刻意不含 `password`，避免未传密码的请求把密码置空 |
| **工作经历全量覆盖** | 先删除该员工全部旧经历，再插入 `experienceList`；传空数组即清空 |
| `id` 必填 | 否则 `员工ID不能为空!` |

### 5.6 批量删除员工

`DELETE /employees?ids=1,2,3` ｜ 权限：`employee:delete`

| 参数 | 说明 |
|---|---|
| `ids` | 员工ID，多个用逗号分隔（支持 `ids=1&ids=2` 写法） |

**业务约束**

| 失败提示 | 条件 |
|---|---|
| `缺少必填参数: ids!` | 完全没有传 `ids` 参数 |
| `请选择要删除的员工!` | 传了 `ids` 但为空 |
| `选中的员工中有正在担任班主任的, 不能删除! 请先在班级管理中更换班主任。` | 选中的员工被 `course_class.head_teacher_id` 引用 |

删除员工时，其工作经历由外键 `ON DELETE CASCADE` 自动清理。

### 5.7 员工下拉框

`GET /employees/list` ｜ 权限：**仅需登录**

供班级表单选择班主任使用。**只返回 `id` 与 `name`**，不返回手机号、薪资等字段。

```json
{ "code": 1, "msg": "success", "data": [ { "id": 1, "name": "张伟" } ] }
```

### 5.8 查询员工角色

`GET /employees/{id}/roles` ｜ 权限：`employee:list`

响应 `data`：角色ID数组，如 `[3]`

### 5.9 为员工分配角色

`PUT /employees/{id}/roles` ｜ 权限：`employee:assign-role`

**请求体**：角色ID数组（**全量覆盖**，传 `[]` 即清空该员工所有角色）

```json
[3]
```

**可能的失败**：`员工不存在!`

> 被改员工若已登录，其权限**立即变化**（权限每次请求实时查库，不存在旧令牌继续生效的问题）。

---

## 6. 部门管理

**Department（部门）**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 主键 |
| `name` | string | 部门名称，非空、唯一、≤20 字符 |
| `createTime` / `updateTime` | datetime | 创建/修改时间 |

| 接口 | 方法与路径 | 权限 | 说明 |
|---|---|---|---|
| 查询全部 | `GET /departments` | `department:list` 或 `employee:list` | 不分页。员工表单的部门下拉框也用它，故放宽 |
| 查询单个 | `GET /departments/{id}` | `department:list` | |
| 新增 | `POST /departments` | `department:create` | 请求体 `{"name":"教研部"}` |
| 修改 | `PUT /departments` | `department:update` | 请求体 `{"id":1,"name":"教研一部"}` |
| 删除 | `DELETE /departments?id=1` | `department:delete` | 参数名是单数 `id` |

**删除约束**：部门下有员工时返回 `对不起，当前部门下有员工，不能直接删除！`

**重复名称**：`教研部已存在!`（由唯一键异常转换而来）

---

## 7. 班级管理

### 7.1 字段字典

**CourseClass（班级）**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 主键 |
| `name` | string | 班级名称，非空、唯一、≤30 字符 |
| `room` | string | 教室 |
| `beginDate` | date | 开课时间，必填 |
| `endDate` | date | 结课时间，必填，**必须晚于开课时间** |
| `headTeacherId` | int | 班主任ID（可为空） |
| `subject` | int | 学科，见 [12.1](#121-枚举字典) |
| `createTime` / `updateTime` | datetime | |
| `headTeacherName` | string | 班主任姓名（关联查询，只读） |
| `status` | string | **班级状态，按日期实时计算，不入库** |

`status` 计算规则（`ClazzServiceImpl`）：

| 条件 | 状态 |
|---|---|
| 今天 < `beginDate` | `未开班` |
| 今天 > `endDate` | `已结课` |
| 其他 | `在读中` |

> 状态不入库的好处是不用定时任务；代价是**无法按状态做数据库层筛选**。

### 7.2 分页查询班级

`GET /course-classes` ｜ 权限：`class:list`

| 参数 | 类型 | 说明 |
|---|---|---|
| `name` | string | 班级名称模糊匹配 |
| `begin` / `end` | date | **结课时间**区间（含当天），必须同时传 |
| `page` / `pageSize` | int | 分页 |

排序：`update_time DESC, id DESC`

### 7.3 新增 / 修改班级

`POST /course-classes` ｜ 权限：`class:create`
`PUT /course-classes` ｜ 权限：`class:update`（请求体需带 `id`）

```json
{
  "name": "JavaEE就业2026-04期",
  "room": "A102",
  "beginDate": "2026-10-01",
  "endDate": "2027-04-01",
  "headTeacherId": 5,
  "subject": 1
}
```

**校验规则**

| 规则 | 失败提示 |
|---|---|
| 名称非空、≤30 字符、唯一 | `班级名称不能为空!` / `JavaEE就业2026-04期已存在!` |
| 开课时间必填 | `开课时间不能为空!` |
| 结课时间必填且晚于开课时间 | `结课时间必须晚于开课时间!` |
| 学科必填 | `请选择学科!` |
| 班主任若传则必须存在 | `所选班主任不存在, 请重新选择!` |

> 数据库层还有一条 `CHECK (end_date > begin_date)` 兜底，即使绕过服务层也写不进非法日期。

### 7.4 删除班级

`DELETE /course-classes/{id}` ｜ 权限：`class:delete`

**约束**：班级下有学员时返回 `对不起, 该班级下有学生, 不能直接删除`

### 7.5 班级下拉框

`GET /course-classes/list` ｜ 权限：`class:list` / `student:list` / `student:create` / `student:update` 任一

供学员表单选择所属班级使用。任何学员模块的写操作也需要它，故放宽为多权限任一。

---

## 8. 学员管理

### 8.1 字段字典

**Student（学员）**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 主键 |
| `name` | string | 姓名，非空、≤10 字符 |
| `studentNo` | string | 学号，**必须恰好 10 位**，唯一 |
| `gender` | int | 性别，1:男 2:女 |
| `phone` | string | 手机号，`^1[3-9]\d{9}$`，唯一 |
| `idCard` | string | 身份证号，**18 位数字**，唯一 |
| `isCollege` | int | 是否院校学员，1:是 0:否，必填 |
| `address` | string | 联系地址 |
| `degree` | int | 最高学历，见 [12.1](#121-枚举字典) |
| `graduationDate` | date | 毕业时间 |
| `classId` | int | 所属班级ID，**必填且必须存在** |
| `violationCount` | int | 违纪次数（新增时服务端置 0） |
| `violationScore` | int | 违纪扣分（新增时服务端置 0） |
| `createTime` / `updateTime` | datetime | |
| `courseClassName` | string | 班级名称（关联查询，只读） |

### 8.2 分页查询学员

`GET /students` ｜ 权限：`student:list`

| 参数 | 类型 | 说明 |
|---|---|---|
| `name` | string | 姓名模糊匹配 |
| `degree` | int | 学历精确匹配 |
| `classId` | int | 班级ID精确匹配 |
| `page` / `pageSize` | int | 分页 |

排序：`update_time DESC, id DESC`

### 8.3 查询学员详情

`GET /students/{id}` ｜ 权限：`student:list`

### 8.4 新增学员

`POST /students` ｜ 权限：`student:create`

```json
{
  "name": "王小明",
  "studentNo": "2026000011",
  "gender": 1,
  "phone": "13900000011",
  "idCard": "110101200201010012",
  "isCollege": 1,
  "address": "北京市海淀区中关村大街1号",
  "degree": 4,
  "graduationDate": "2024-06-30",
  "classId": 1
}
```

**校验规则**

| 规则 | 失败提示 |
|---|---|
| 姓名非空、≤10 字符 | `姓名不能为空!` |
| 学号非空且**恰好 10 位** | `学号长度必须为10个字符!` |
| 性别必填且为 1/2 | `请选择正确的性别!` |
| 手机号格式 | `请输入合法的手机号!` |
| 身份证 18 位数字 | `请输入合法的身份证号!` |
| `isCollege` 必填 | `请选择是否为院校学员!` |
| **`classId` 必填且存在** | `请选择所属班级!` / `所选班级不存在, 请重新选择!` |

> `violationCount` / `violationScore` 由服务端固定置 0，客户端传值会被忽略。

### 8.5 修改学员

`PUT /students` ｜ 权限：`student:update`（请求体需带 `id`）

校验规则同新增。注意这是**全量更新**，`violationCount` / `violationScore` 也需一并回传
（前端编辑弹窗的数据来自详情接口，已包含这两个字段）。

### 8.6 批量删除学员

`DELETE /students/{ids}` ｜ 权限：`student:delete`

| 参数 | 说明 |
|---|---|
| `ids` | **路径变量**，多个用逗号分隔，如 `DELETE /students/1,2,3` |

失败提示：`请选择要删除的学员!`

> 注意与员工的区别：员工用 `?ids=` 查询参数，学员用路径变量，两个模块不统一（历史原因）。

### 8.7 学员违纪扣分

`PUT /students/violations/{id}/{score}` ｜ 权限：`student:violation`

| 路径参数 | 类型 | 说明 |
|---|---|---|
| `id` | int | 学员ID |
| `score` | int | 本次扣分（累加值，非覆盖值） |

执行逻辑：`violationCount + 1`、`violationScore + score`。

**注意**：`violation_score` 列为 `tinyint unsigned`（上限 255），前端也只允许 1 位数字输入，
扣分总量接近 255 时会因超范围报错。

---

## 9. 报表统计

四个接口都只用于首页/统计页的 ECharts 图表，**无入参、不分页**。

| 接口 | 权限 | 响应 `data` |
|---|---|---|
| `GET /reports/employee-positions` | `report:employee` | `{ positions: [...], counts: [...] }` 两个数组按下标对应 |
| `GET /reports/employee-genders` | `report:employee` | `[{ name: "男", value: 4 }, { name: "女", value: 5 }]` |
| `GET /reports/course-class-student-counts` | `report:student` | `{ courseClassNames: [...], counts: [...] }` |
| `GET /reports/student-degrees` | `report:student` | `[{ name: "本科", value: 4 }, ...]` |

```json
// GET /reports/employee-positions
{
  "code": 1, "msg": "success",
  "data": {
    "positions": ["教研主管", "学工主管", "其他", "讲师", "班主任", "咨询师"],
    "counts": [1, 1, 1, 2, 2, 2]
  }
}
```

> `counts` 使用 `count(student.id)` 而非 `count(*)`，因此**没有学员的班级会正确返回 0**
> （用 `count(*)` 配合 `left join` 会把空班级数成 1）。

---

## 10. 操作日志

### 10.1 分页查询操作日志

`GET /operation-logs/page?page=1&pageSize=15` ｜ 权限：`report:log`

排序：`operation_time DESC, id DESC`

**OperationLog（操作日志）**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 主键 |
| `operatorId` | int | 操作人ID |
| `operatorName` | string | 操作人姓名；登录接口取请求体里的用户名 |
| `operationTime` | datetime | 操作时间 |
| `className` | string | 控制器类名，如 `EmployeeController` |
| `methodName` | string | 方法名，如 `addEmployee` |
| `methodParams` | string | 请求参数（`password` 掩码为 `****`，最长 2000 字符） |
| `returnValue` | string | **操作结果**：成功记 `成功`，失败记 `失败: 原因` |
| `costTime` | long | 耗时（毫秒） |
| `operationIp` | string | 操作IP |

```json
{
  "code": 1, "msg": "success",
  "data": {
    "total": 27,
    "rows": [
      { "id": 27, "operatorId": 2, "operatorName": "李娜", "operationTime": "2026-09-25T13:41:02",
        "className": "CourseClassController", "methodName": "addCourseClass",
        "methodParams": "{\"name\":\"x\"}",
        "returnValue": "失败: 权限不足: 需要【class:create】权限, 请联系管理员!",
        "costTime": 6, "operationIp": "0:0:0:0:0:0:0:1" }
    ]
  }
}
```

### 10.2 记录规则

| 规则 | 说明 |
|---|---|
| 只记写操作 | POST / PUT / DELETE / PATCH；查询接口不记，避免日志表被刷爆 |
| 不记完整响应体 | 只记结论。响应体可能含 **token**，整段落库等于泄漏可用令牌 |
| `password` 掩码 | 请求参数里的 `password` 替换为 `****` |
| 401 不记 | 未登录请求不产生日志 |
| **403 会记** | 越权被拒留痕，便于审计（`returnValue` 能看到缺少哪个权限） |
| 独立事务 | 用 `REQUIRES_NEW`，业务回滚也照样留痕 |
| 失败不影响业务 | 写日志异常只打 error 日志，不影响接口响应 |

---

## 11. 角色与权限管理

### 11.1 字段字典

**Role（角色）**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 主键 |
| `name` | string | 角色名称，非空、唯一、≤20 字符 |
| `code` | string | 角色标识，非空、唯一、≤30 字符，如 `ADMIN` |
| `remark` | string | 备注 |
| `createTime` / `updateTime` | datetime | |

**RoleForm（新增/修改角色的请求体）**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 修改时必填 |
| `name` / `code` / `remark` | string | 同上 |
| `menuIds` | int[] | 该角色拥有的菜单/权限ID（全量覆盖） |

**Menu（菜单/权限）**

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | int | 主键 |
| `parentId` | int | 父级ID，0 表示顶级 |
| `name` | string | 名称 |
| `type` | int | 1:菜单（有 `path`）2:按钮/接口权限 |
| `permissionCode` | string | 权限标识；分组菜单为 `null` |
| `path` | string | 前端路由，仅菜单有值 |
| `sort` | int | 排序 |
| `children` | Menu[] | 子节点（树形返回时才有） |

### 11.2 接口

| 接口 | 方法与路径 | 权限 | 说明 |
|---|---|---|---|
| 角色列表 | `GET /roles` | `role:list` | 不分页 |
| 角色已有权限 | `GET /roles/{id}/menus` | `role:list` | 返回菜单ID数组 |
| 新增角色 | `POST /roles` | `role:create` | 请求体 `RoleForm`，可同时带 `menuIds` |
| 修改角色 | `PUT /roles` | `role:update` | 请求体 `RoleForm`（需 `id`），`menuIds` 全量覆盖 |
| 删除角色 | `DELETE /roles/{id}` | `role:delete` | |
| 分配权限 | `PUT /roles/{id}/menus` | `role:assign-menu` | 请求体为菜单ID数组，全量覆盖 |
| 权限树 | `GET /menus` | `role:list` | 树形，供授权界面渲染 |
| 我的菜单 | `GET /menus/mine` | 仅需登录 | 当前用户可见菜单（扁平，仅 `type=1`） |

**业务约束**

| 失败提示 | 条件 |
|---|---|
| `角色名称或角色标识已存在!` | `name` 或 `code` 与其它角色重复 |
| `不允许修改内置超级管理员的角色标识!` | 修改 `code=ADMIN` 的角色时改了 `code` |
| `内置超级管理员角色不能删除!` | 删除 `code=ADMIN` 的角色 |
| `该角色已分配给 N 名员工, 不能直接删除! 请先解除关联。` | 该角色仍有员工使用 |
| `角色不存在!` | ID 无效 |

### 11.3 示例：新建一个只读审计角色

```bash
TOKEN=<zhangwei 登录后的 token>

# 1) 新建角色, 同时勾选"首页 + 报表"相关权限(菜单ID 1,22,23,24,25)
curl -X POST -H "token: $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"审计员","code":"AUDITOR","remark":"只读报表","menuIds":[1,22,23,24,25]}' \
  http://localhost:8080/roles

# 2) 把该角色分配给员工 9
curl -X PUT -H "token: $TOKEN" -H "Content-Type: application/json" \
  -d '[6]' http://localhost:8080/employees/9/roles
```

> 前端是打包产物、**没有角色管理页面**，该模块只能通过接口使用。
> 若需图形化界面，需要拿到 Vue 源码工程按 `GET /menus` 的树形结构重做。

---

## 12. 文件上传

### 12.1 上传文件

`POST /files/upload` ｜ 权限：`employee:create` 或 `employee:update`

`multipart/form-data`，字段名 **`file`**。

```bash
curl -X POST -H "token: $TOKEN" \
  -F "file=@D:/code/MyProject/photo.jpg;type=image/jpeg" \
  http://localhost:8080/files/upload
```

**响应**：`data` 为可访问的 URL 字符串

```json
{ "code": 1, "msg": "success", "data": "/api/uploads/2026/09/52cc09e0c743484f9a4994b71b8c9a1e.jpg" }
```

**校验规则**

| 规则 | 失败提示 |
|---|---|
| 文件非空 | `上传文件不能为空!` |
| 扩展名属于 `jpg/jpeg/png/gif/bmp/webp` | `只支持上传 jpg/jpeg/png/gif/bmp/webp 格式的图片!` |
| Content-Type 以 `image/` 开头 | `只支持上传图片文件!` |
| 大小 ≤ 10MB | `图片大小不能超过 10MB!` |

**存储方式**

- 落地到本地磁盘：`{file.storage.local-dir}/{yyyy}/{MM}/{uuid}.{ext}`
- 文件名用 UUID，避免重名与路径穿越；落盘前做路径归一化校验
- 对外通过 `GET /uploads/**` 提供静态访问（**免鉴权**，因为 `<img>` 无法带请求头）
- 返回的 URL 带 `file.storage.access-prefix` 前缀（默认 `/api/uploads`，适配 nginx 的 `/api` 代理）

**上传后如何访问**

| 场景 | URL |
|---|---|
| 经 nginx（前端实际使用） | `http://localhost:90/api/uploads/2026/09/xxx.jpg` |
| 直连后端 | `http://localhost:8080/uploads/2026/09/xxx.jpg` |

> 注意：返回的 URL 是带 `/api` 前缀的。若前端改为直连后端，需把
> `file.storage.access-prefix` 改成 `/uploads`。

---

## 13. 附录

### 13.1 枚举字典

**`gender` 性别**

| 值 | 含义 |
|---|---|
| 1 | 男 |
| 2 | 女 |

**`position` 职位（员工）**

| 值 | 含义 |
|---|---|
| 1 | 班主任 |
| 2 | 讲师 |
| 3 | 学工主管 |
| 4 | 教研主管 |
| 5 | 咨询师 |
| 6 | 其他 |

**`degree` 最高学历（学员）**

| 值 | 含义 |
|---|---|
| 1 | 初中 |
| 2 | 高中 |
| 3 | 大专 |
| 4 | 本科 |
| 5 | 硕士 |
| 6 | 博士 |

**`subject` 学科（班级）**

| 值 | 含义 |
|---|---|
| 1 | Java |
| 2 | 前端 |
| 3 | 大数据 |
| 4 | Python |
| 5 | Go |
| 6 | 嵌入式 |

**`isCollege` 是否院校学员**：1 = 是，0 = 否

**班级 `status`**：`未开班` / `在读中` / `已结课`（按日期实时计算）

**菜单 `type`**：1 = 菜单，2 = 按钮/接口权限

> 这些枚举目前**硬编码**在 SQL 的 `case when`、Java 与前端三处，没有数据字典表。
> 新增一个学科需要同时改三处，属于已知待办。

### 13.2 常见错误信息对照

| `msg` | 出现场景 |
|---|---|
| `程序出错了!` | 未预期的服务端异常，日志里能看到完整堆栈 |
| `请求体格式错误, 请检查 JSON 是否合法!` | 请求体不是合法 JSON（缺引号、多逗号等） |
| `缺少必填参数: xxx!` | 缺少必填的 query 参数，如批量删除未传 `ids` |
| `权限不足: 需要【xxx】权限, 请联系管理员!` | 403，缺少对应权限 |
| `用户名不存在!` / `密码错误!` | 登录失败 |
| `XXXX已存在!` | 违反唯一键（用户名、手机号、部门名、班级名、学号、身份证号等） |
| `请选择要删除的员工!` / `请选择要删除的学员!` | 批量删除未传 ID |
| `对不起，当前部门下有员工，不能直接删除！` | 部门删除约束 |
| `对不起, 该班级下有学生, 不能直接删除` | 班级删除约束 |
| `选中的员工中有正在担任班主任的, 不能删除! ...` | 员工删除约束 |

### 13.3 调试建议

```bash
# 1) 取 token
TOKEN=$(curl -s -X POST -H "Content-Type: application/json" \
  -d '{"username":"zhangwei","password":"123456"}' \
  http://localhost:8080/login | sed -E 's/.*"token":"([^"]+)".*/\1/')

# 2) 带 token 调用
curl -s -H "token: $TOKEN" "http://localhost:8080/employees?page=1&pageSize=5"

# 3) 查看响应头与状态码(排查 401/403)
curl -i -H "token: $TOKEN" http://localhost:8080/departments
```

`application.yml` 中 `mybatis.configuration.log-impl` 为 `StdOutImpl`，**控制台会打印所有 SQL 与参数**，
排查取数问题非常方便，但生产环境应关闭（登录 SQL 会把密码打进日志）。

### 13.4 接口设计上的已知不一致

这些是历史遗留，文档如实记录，`README` 的「已知待办」中也有对应条目：

| 不一致点 | 现状 |
|---|---|
| 删除接口参数风格 | 员工用 `?ids=`、部门用 `?id=`、班级用 `/{id}`、学员用 `/{ids}` 四种写法 |
| URL 复数 | 已统一为 `/employees`、`/departments`、`/course-classes`，但 `clazz` 一词的历史痕迹仍在部分类名中 |
| 入职日期与结课时间查询 | 都用 `begin`/`end` 作为参数名，语义由具体模块决定，容易混淆 |
| 全量更新 | `PUT /employees`、`PUT /students` 均为全量覆盖，未传的字段会被覆盖为空 |
| 业务失败用 200 | 见 [1.2](#12-http-状态码) |
