package com.qiming.controller;

import com.qiming.annotation.RequiresPermission;
import com.qiming.pojo.Employee;
import com.qiming.pojo.EmployeeQueryParam;
import com.qiming.pojo.PageResult;
import com.qiming.pojo.Result;
import com.qiming.service.EmployeeService;
import com.qiming.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private RoleService roleService;

    /**
     * 分页查询员工列表
     */
    @GetMapping
    @RequiresPermission("employee:list")
    public Result page(EmployeeQueryParam employeeQueryParam) {
        PageResult<Employee> pageResult = employeeService.page(employeeQueryParam);
        return Result.success(pageResult);
    }

    /**
     * 添加员工
     */
    @PostMapping
    @RequiresPermission("employee:create")
    public Result addEmployee(@RequestBody Employee employee) {
        log.info("增加员工:{}", employee);
        employeeService.addEmployee(employee);
        return Result.success();
    }

    /**
     * 删除员工
     */
    @DeleteMapping
    @RequiresPermission("employee:delete")
    public Result deleteEmployee(@RequestParam List<Integer> ids) {
        log.info("删除员工:{}", ids);
        employeeService.deleteEmployee(ids);
        return Result.success();
    }

    /**
     * 根据id查找员工
     */
    @GetMapping("/{id}")
    @RequiresPermission("employee:list")
    public Result getEmployeeById(@PathVariable Integer id) {
        log.info("查找员工:{}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 更新员工
     */
    @PutMapping
    @RequiresPermission("employee:update")
    public Result updateEmployee(@RequestBody Employee employee) {
        log.info("更新员工:{}", employee);
        employeeService.updateEmployee(employee);
        return Result.success();
    }

    /**
     * 查询所有员工(下拉框; 只返回 id + name)
     */
    @GetMapping("/list")
    public Result findAll() {
        List<Employee> employeeList = employeeService.findAll();
        return Result.success(employeeList);
    }

    /**
     * 查询员工拥有的角色ID
     */
    @GetMapping("/{id}/roles")
    @RequiresPermission("employee:list")
    public Result listRoleIds(@PathVariable Integer id) {
        return Result.success(roleService.listRoleIdsByEmployeeId(id));
    }

    /**
     * 为员工分配角色(RBAC: 员工 -> 角色)
     */
    @PutMapping("/{id}/roles")
    @RequiresPermission("employee:assign-role")
    public Result assignRoles(@PathVariable Integer id, @RequestBody List<Integer> roleIds) {
        log.info("为员工{}分配角色:{}", id, roleIds);
        roleService.assignRolesToEmployee(id, roleIds);
        return Result.success();
    }

}
