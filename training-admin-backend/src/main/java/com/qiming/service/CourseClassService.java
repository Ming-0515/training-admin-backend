package com.qiming.service;

import com.qiming.pojo.CourseClass;
import com.qiming.pojo.CourseClassQueryParam;
import com.qiming.pojo.PageResult;

import java.util.List;

public interface CourseClassService {

    /**
     * 分页查询班级列表
     */
    PageResult<CourseClass> page(CourseClassQueryParam courseClassQueryParam);

    /**
     * 添加班级
     */
    void addCourseClass(CourseClass courseClass);

    /**
     * 根据ID查询班级
     */
    CourseClass getCourseClassById(Integer id);

    /**
     * 更新班级
     */
    void updateCourseClass(CourseClass courseClass);

    /**
     * 删除班级
     */
    void deleteCourseClass(Integer id);

    /**
     * 查询所有班级
     */
    List<CourseClass> findAll();
}
