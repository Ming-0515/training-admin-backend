package com.qiming.service;

import com.qiming.pojo.Role;
import com.qiming.pojo.RoleForm;

import java.util.List;

public interface RoleService {

    /** 超级管理员角色标识, 拥有该角色的用户放行全部接口 */
    String ADMIN_ROLE_CODE = "ADMIN";

    List<Role> listAll();

    Role getById(Integer id);

    List<Integer> listMenuIdsByRoleId(Integer roleId);

    void addRole(RoleForm form);

    void updateRole(RoleForm form);

    void deleteRole(Integer id);

    void assignMenus(Integer roleId, List<Integer> menuIds);

    List<Role> listRolesByEmployeeId(Integer employeeId);

    List<Integer> listRoleIdsByEmployeeId(Integer employeeId);

    void assignRolesToEmployee(Integer employeeId, List<Integer> roleIds);

    /** 是否超级管理员 */
    boolean isAdmin(Integer employeeId);

}
