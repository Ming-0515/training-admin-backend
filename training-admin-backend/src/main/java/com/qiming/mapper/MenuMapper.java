package com.qiming.mapper;

import com.qiming.pojo.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenuMapper {

    /**
     * 查询全部菜单/权限(不分页, 用于权限分配界面)
     */
    List<Menu> listAll();

    /**
     * 查询某个员工拥有的全部权限标识
     */
    List<String> listPermissionCodesByEmployeeId(Integer employeeId);

    /**
     * 查询某个员工可见的菜单(type=1, 即带前端路由的菜单)
     */
    List<Menu> listMenusByEmployeeId(Integer employeeId);

}
