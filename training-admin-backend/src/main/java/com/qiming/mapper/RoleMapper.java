package com.qiming.mapper;

import com.qiming.pojo.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {

    /**
     * 查询全部角色
     */
    List<Role> listAll();

    /**
     * 根据ID查询角色
     */
    Role getById(Integer id);

    /**
     * 统计同名或同标识的角色数量(排除指定ID, 用于唯一性校验)
     */
    int countByNameOrCode(@Param("name") String name, @Param("code") String code, @Param("excludeId") Integer excludeId);

    /**
     * 新增角色
     */
    void insert(Role role);

    /**
     * 修改角色
     */
    void update(Role role);

    /**
     * 删除角色
     */
    void deleteById(Integer id);

    /**
     * 统计该角色已分配给多少名员工
     */
    int countEmployeeByRoleId(Integer roleId);

    /**
     * 清空角色的菜单权限
     */
    void deleteRoleMenus(Integer roleId);

    /**
     * 查询角色拥有的菜单ID
     */
    List<Integer> listMenuIdsByRoleId(Integer roleId);

    /**
     * 为角色批量分配菜单权限
     */
    void insertRoleMenus(@Param("roleId") Integer roleId, @Param("menuIds") List<Integer> menuIds);

    /**
     * 查询员工拥有的角色
     */
    List<Role> listRolesByEmployeeId(Integer employeeId);

    /**
     * 查询员工拥有的角色ID
     */
    List<Integer> listRoleIdsByEmployeeId(Integer employeeId);

    /**
     * 清空员工的角色
     */
    void deleteEmployeeRoles(Integer employeeId);

    /**
     * 为员工批量分配角色
     */
    void insertEmployeeRoles(@Param("employeeId") Integer employeeId, @Param("roleIds") List<Integer> roleIds);

}
