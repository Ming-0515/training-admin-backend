package com.qiming.controller;

import com.qiming.annotation.RequiresPermission;
import com.qiming.pojo.Menu;
import com.qiming.pojo.Result;
import com.qiming.service.MenuService;
import com.qiming.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单与权限
 */
@Slf4j
@RestController
@RequestMapping("/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 全部菜单/权限(树形), 用于权限分配界面
     */
    @GetMapping
    @RequiresPermission("role:list")
    public Result listAll() {
        log.info("查询全部菜单权限");
        return Result.success(menuService.listAllTree());
    }

    /**
     * 当前登录用户可见的菜单。
     * 由登录接口下发 permissions 之外, 单独提供此接口便于前端刷新菜单;
     * 任何已登录用户都可以查询自己的菜单, 无需额外权限。
     */
    @GetMapping("/mine")
    public Result listMine() {
        Integer employeeId = UserContext.getEmployeeId();
        List<Menu> menus = menuService.listMenusByEmployeeId(employeeId);
        return Result.success(menus);
    }

}
