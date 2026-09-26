package com.qiming.service;


import com.qiming.pojo.Department;

import java.util.List;

public interface DepartmentService {

    /**
     * 查询全部部门数据
     */
    List<Department> findAll();

    /**
     * 删除部门
     */
    void deleteById(Integer id);

    /**
     * 添加部门
     */
    void addDepartment(Department department);

    /**
     * 根据id查询部门
     */
    Department findById(Integer id);

    /**
     * 更新部门
     */
    void updateDepartment(Department department);

}
