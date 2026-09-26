import request from "@/utils/request";

/**
 * 菜单 / 权限 - 接口
 * 后端: MenuController, 路径前缀 /menus
 * menu 一张表承载两类数据: type=1 菜单(有 path) / type=2 按钮接口权限
 */

//全部菜单权限树(树形, 授权界面用, 权限: role:list)
export const queryAllApi = () => request.get('/menus');

//当前登录人可见的菜单(扁平, 仅 type=1, 仅需登录) —— 侧边栏菜单就靠它
export const queryMineApi = () => request.get('/menus/mine');
