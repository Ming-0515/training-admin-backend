package com.qiming.service.impl;

import com.qiming.mapper.DepartmentMapper;
import com.qiming.mapper.EmployeeMapper;
import com.qiming.pojo.Department;
import com.qiming.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public List<Department> findAll() {
        return departmentMapper.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        // 检查部门下是否有员工
        if (employeeMapper.countByDepartmentId(id) > 0) {
            throw new IllegalArgumentException("对不起，当前部门下有员工，不能直接删除！");
        }

        departmentMapper.deleteById(id);
    }

    @Override
    public void addDepartment(Department department) {
        // 设置创建时间和更新时间
        department.setCreateTime(LocalDateTime.now());
        department.setUpdateTime(LocalDateTime.now());
        departmentMapper.addDepartment(department);
    }

    @Override
    public Department findById(Integer id) {
        return departmentMapper.findById(id);
    }

    @Override
    public void updateDepartment(Department department) {
        // 设置更新时间
        department.setUpdateTime(LocalDateTime.now());
        departmentMapper.updateDepartment(department);
    }

}
