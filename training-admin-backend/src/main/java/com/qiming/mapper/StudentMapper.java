package com.qiming.mapper;

import com.github.pagehelper.Page;
import com.qiming.pojo.Student;
import com.qiming.pojo.StudentQueryParam;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentMapper {

    /**
     * 查询学员分页数据
     */
    Page<Student> page(StudentQueryParam studentQueryParam);

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
}
