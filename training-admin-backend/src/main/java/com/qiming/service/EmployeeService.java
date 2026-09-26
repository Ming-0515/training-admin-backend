package com.qiming.service;

import com.qiming.pojo.Employee;
import com.qiming.pojo.EmployeeQueryParam;
import com.qiming.pojo.LoginInfo;
import com.qiming.pojo.PageResult;

import java.util.List;


public interface EmployeeService {

    /**
     * 分页查询员工列表
     */
    public PageResult<Employee> page(EmployeeQueryParam employeeQueryParam);

    /**
     * 添加员工
     */
    void addEmployee(Employee employee);

    /**
     * 删除员工
     */
    void deleteEmployee(List<Integer> ids);

    /**
     * 根据id查找员工
     */
    Employee getById(Integer id);

    /**
     * 更新员工
     */
    void updateEmployee(Employee employee);

    /**
     * 查询所有员工
     */
    List<Employee> findAll();

    /**
     * 登录
     */
    LoginInfo login(Employee employee);
}
