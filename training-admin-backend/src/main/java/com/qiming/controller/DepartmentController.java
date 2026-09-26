package com.qiming.controller;

import com.qiming.annotation.RequiresPermission;

import com.qiming.pojo.Department;
import com.qiming.pojo.Result;
import com.qiming.service.DepartmentService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/departments")
@RestController
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    /**
     * 查询全部部门数据
     */
    @GetMapping
    @RequiresPermission({"department:list", "employee:list"})
    public Result findAll() {
        log.info("查询全部部门数据");
        List<Department> departmentList = departmentService.findAll();
        return Result.success(departmentList);
    }

    /**
     * 删除部门
     */
    @DeleteMapping
    @RequiresPermission("department:delete")
    public Result deleteById(Integer id) {
        log.info("删除部门{}", id);
        departmentService.deleteById(id);
        return Result.success();
    }

    /**
     * 添加部门
     */
    @PostMapping
    @RequiresPermission("department:create")
    public Result addDepartment(@RequestBody Department department) {
        log.info("添加部门{}", department);
        departmentService.addDepartment(department);
        return Result.success();
    }

    /**
     * 根据id查询部门
     */
    @GetMapping("/{id}")
    @RequiresPermission("department:list")
    public Result findById(@PathVariable Integer id) {
        log.info("根据id查询部门{}", id);
        Department department = departmentService.findById(id);
        return Result.success(department);
    }

    /**
     * 更新部门
     */
    @PutMapping
    @RequiresPermission("department:update")
    public Result updateDepartment(@RequestBody Department department) {
        log.info("更新部门{}", department);
        departmentService.updateDepartment(department);
        return Result.success();
    }

}
