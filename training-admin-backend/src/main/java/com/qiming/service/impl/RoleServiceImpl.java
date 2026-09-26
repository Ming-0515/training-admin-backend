package com.qiming.service.impl;

import com.qiming.mapper.EmployeeMapper;
import com.qiming.mapper.RoleMapper;
import com.qiming.pojo.Employee;
import com.qiming.pojo.Role;
import com.qiming.pojo.RoleForm;
import com.qiming.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public List<Role> listAll() {
        return roleMapper.listAll();
    }

    @Override
    public Role getById(Integer id) {
        return roleMapper.getById(id);
    }

    @Override
    public List<Integer> listMenuIdsByRoleId(Integer roleId) {
        return roleMapper.listMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRole(RoleForm form) {
        checkRoleForm(form, null);

        Role role = new Role();
        role.setName(form.getName());
        role.setCode(form.getCode());
        role.setRemark(form.getRemark());
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(role);

        // 新增角色时同步保存勾选的菜单权限
        if (!CollectionUtils.isEmpty(form.getMenuIds())) {
            roleMapper.insertRoleMenus(role.getId(), form.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleForm form) {
        if (form == null || form.getId() == null) {
            throw new IllegalArgumentException("角色ID不能为空!");
        }
        Role exists = roleMapper.getById(form.getId());
        if (exists == null) {
            throw new IllegalArgumentException("角色不存在!");
        }
        checkRoleForm(form, form.getId());

        // 不允许修改内置超级管理员的标识, 否则会让 ADMIN 兜底放行失效
        if (ADMIN_ROLE_CODE.equals(exists.getCode()) && !ADMIN_ROLE_CODE.equals(form.getCode())) {
            throw new IllegalArgumentException("不允许修改内置超级管理员的角色标识!");
        }

        Role role = new Role();
        role.setId(form.getId());
        role.setName(form.getName());
        role.setCode(form.getCode());
        role.setRemark(form.getRemark());
        role.setUpdateTime(LocalDateTime.now());
        roleMapper.update(role);

        // 先清空再重建, 前端提交的就是最终的权限集合
        roleMapper.deleteRoleMenus(form.getId());
        if (!CollectionUtils.isEmpty(form.getMenuIds())) {
            roleMapper.insertRoleMenus(form.getId(), form.getMenuIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("角色ID不能为空!");
        }
        Role exists = roleMapper.getById(id);
        if (exists == null) {
            throw new IllegalArgumentException("角色不存在!");
        }
        if (ADMIN_ROLE_CODE.equals(exists.getCode())) {
            throw new IllegalArgumentException("内置超级管理员角色不能删除!");
        }
        int used = roleMapper.countEmployeeByRoleId(id);
        if (used > 0) {
            throw new IllegalArgumentException("该角色已分配给 " + used + " 名员工, 不能直接删除! 请先解除关联。");
        }
        roleMapper.deleteRoleMenus(id);
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Integer roleId, List<Integer> menuIds) {
        if (roleId == null || roleMapper.getById(roleId) == null) {
            throw new IllegalArgumentException("角色不存在!");
        }
        roleMapper.deleteRoleMenus(roleId);
        if (!CollectionUtils.isEmpty(menuIds)) {
            roleMapper.insertRoleMenus(roleId, menuIds);
        }
    }

    @Override
    public List<Role> listRolesByEmployeeId(Integer employeeId) {
        return roleMapper.listRolesByEmployeeId(employeeId);
    }

    @Override
    public List<Integer> listRoleIdsByEmployeeId(Integer employeeId) {
        return roleMapper.listRoleIdsByEmployeeId(employeeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToEmployee(Integer employeeId, List<Integer> roleIds) {
        if (employeeId == null) {
            throw new IllegalArgumentException("员工ID不能为空!");
        }
        Employee employee = employeeMapper.getById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("员工不存在!");
        }
        roleMapper.deleteEmployeeRoles(employeeId);
        if (!CollectionUtils.isEmpty(roleIds)) {
            roleMapper.insertEmployeeRoles(employeeId, roleIds);
        }
    }

    @Override
    public boolean isAdmin(Integer employeeId) {
        if (employeeId == null) {
            return false;
        }
        return roleMapper.listRolesByEmployeeId(employeeId).stream()
                .anyMatch(role -> ADMIN_ROLE_CODE.equals(role.getCode()));
    }

    /**
     * 角色表单校验
     */
    private void checkRoleForm(RoleForm form, Integer excludeId) {
        if (form == null) {
            throw new IllegalArgumentException("角色信息不能为空!");
        }
        if (!StringUtils.hasText(form.getName())) {
            throw new IllegalArgumentException("角色名称不能为空!");
        }
        if (form.getName().length() > 20) {
            throw new IllegalArgumentException("角色名称长度不能超过20个字符!");
        }
        if (!StringUtils.hasText(form.getCode())) {
            throw new IllegalArgumentException("角色标识不能为空!");
        }
        if (form.getCode().length() > 30) {
            throw new IllegalArgumentException("角色标识长度不能超过30个字符!");
        }
        // 提前做唯一性校验, 避免直接抛数据库唯一键异常(提示不友好)
        if (roleMapper.countByNameOrCode(form.getName(), form.getCode(), excludeId) > 0) {
            throw new IllegalArgumentException("角色名称或角色标识已存在!");
        }
    }

}
