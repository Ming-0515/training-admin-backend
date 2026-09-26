package com.qiming.service;

import com.qiming.pojo.PageResult;
import com.qiming.pojo.Student;
import com.qiming.pojo.StudentQueryParam;

public interface StudentService {

    /**
     * 查询学员分页数据
     */
    PageResult<Student> page(StudentQueryParam studentQueryParam);

    /**
     * 添加学员
     */
    void addStudent(Student student);

    /**
     * 根据id查询学员详情
     */
    Student getStudentById(Integer id);

    /**
     * 更新学员信息
     */
    void updateStudent(Student student);

    /**
     * 删除学员
     */
    void deleteStudentById(Integer[] ids);

    /**
     * 更新学员违纪扣分
     */
    void updateStudentViolation(Integer id, Short score);
}
