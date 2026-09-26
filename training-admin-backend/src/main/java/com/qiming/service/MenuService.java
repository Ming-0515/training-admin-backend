package com.qiming.service;

import com.qiming.pojo.Menu;

import java.util.List;
import java.util.Set;

public interface MenuService {

    /**
     * 全部菜单/权限(扁平列表)
     */
    List<Menu> listAll();

    /**
     * 全部菜单/权限(树形, 用于权限分配界面)
     */
    List<Menu> listAllTree();

    /**
     * 某个员工拥有的全部权限标识
     */
    Set<String> listPermissionCodes(Integer employeeId);

    /**
     * 某个员工可见的菜单(带前端路由)
     */
    List<Menu> listMenusByEmployeeId(Integer employeeId);

}
