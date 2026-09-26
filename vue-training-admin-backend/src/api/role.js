import request from "@/utils/request";

/**
 * 角色管理 - 接口
 * 后端: RoleController, 路径前缀 /roles
 * 角色与权限是多对多: 角色 <- role_menu -> 菜单/权限
 */

//查询全部角色(不分页)
export const queryAllApi = () => request.get('/roles');

//查询角色已拥有的菜单/权限ID数组
export const queryMenuIdsApi = (id) => request.get(`/roles/${id}/menus`);

//新增角色(可在 RoleForm 里同时带 menuIds 一次性授权)
export const addApi = (role) => request.post('/roles', role);

//修改角色(需带 id, menuIds 全量覆盖)
export const updateApi = (role) => request.put('/roles', role);

//删除角色
export const deleteByIdApi = (id) => request.delete(`/roles/${id}`);

//为角色分配权限(菜单ID数组, 全量覆盖)
export const assignMenusApi = (id, menuIds) => request.put(`/roles/${id}/menus`, menuIds);
