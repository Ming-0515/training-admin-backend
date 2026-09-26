import request from "@/utils/request";

/**
 * 员工管理 - 接口
 * 后端: EmployeeController, 路径前缀 /employees
 * 注意: password 只写不读(响应里永远不返回), 修改员工时也不回写密码
 */

//分页查询员工(begin/end 筛选的是入职日期, 必须同时传)
export const queryPageApi = (name, gender, begin, end, page, pageSize) =>
  request.get(`/employees?name=${name}&gender=${gender}&begin=${begin}&end=${end}&page=${page}&pageSize=${pageSize}`);

//新增员工(password 不传时服务端补默认值 123456)
export const addApi = (emp) => request.post('/employees', emp);

//根据ID查询(含工作经历 experienceList)
export const queryInfoApi = (id) => request.get(`/employees/${id}`);

//修改员工(工作经历全量覆盖, 传空数组即清空)
export const updateApi = (emp) => request.put('/employees', emp);

//批量删除员工(注意: 查询参数 ?ids=1,2,3, 与学员的路径变量不同)
export const deleteApi = (ids) => request.delete(`/employees?ids=${ids}`);

//员工下拉框(班级表单选班主任用), 只返回 id 与 name
export const queryAllApi = () => request.get('/employees/list');

//查询员工拥有的角色ID数组
export const queryRolesApi = (id) => request.get(`/employees/${id}/roles`);

//为员工分配角色(角色ID数组, 全量覆盖)
export const assignRolesApi = (id, roleIds) => request.put(`/employees/${id}/roles`, roleIds);
