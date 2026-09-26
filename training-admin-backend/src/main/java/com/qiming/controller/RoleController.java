package com.qiming.controller;

import com.qiming.annotation.RequiresPermission;
import com.qiming.pojo.Result;
import com.qiming.pojo.Role;
import com.qiming.pojo.RoleForm;
import com.qiming.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理(权限分配)
 * 注意: 前端为打包产物、没有角色管理页面, 该模块通过接口使用, 详见 README。
 */
@Slf4j
@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 查询全部角色
     */
    @GetMapping
    @RequiresPermission("role:list")
    public Result listAll() {
        log.info("查询全部角色");
        List<Role> roles = roleService.listAll();
        return Result.success(roles);
    }

    /**
     * 查询角色拥有的菜单权限ID
     */
    @GetMapping("/{id}/menus")
    @RequiresPermission("role:list")
    public Result listMenuIds(@PathVariable Integer id) {
        log.info("查询角色{}的菜单权限", id);
        return Result.success(roleService.listMenuIdsByRoleId(id));
    }

    /**
     * 新增角色(可同时分配菜单权限)
     */
    @PostMapping
    @RequiresPermission("role:create")
    public Result addRole(@RequestBody RoleForm form) {
        log.info("新增角色:{}", form);
        roleService.addRole(form);
        return Result.success();
    }

    /**
     * 修改角色(同时重置菜单权限)
     */
    @PutMapping
    @RequiresPermission("role:update")
    public Result updateRole(@RequestBody RoleForm form) {
        log.info("修改角色:{}", form);
        roleService.updateRole(form);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("role:delete")
    public Result deleteRole(@PathVariable Integer id) {
        log.info("删除角色:{}", id);
        roleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 为角色分配菜单权限
     */
    @PutMapping("/{id}/menus")
    @RequiresPermission("role:assign-menu")
    public Result assignMenus(@PathVariable Integer id, @RequestBody List<Integer> menuIds) {
        log.info("为角色{}分配权限:{}", id, menuIds);
        roleService.assignMenus(id, menuIds);
        return Result.success();
    }

}
