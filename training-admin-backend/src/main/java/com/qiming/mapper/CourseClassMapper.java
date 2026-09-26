package com.qiming.mapper;

import com.github.pagehelper.Page;
import com.qiming.pojo.CourseClass;
import com.qiming.pojo.CourseClassQueryParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CourseClassMapper {

    /**
     * 分页查询班级
     */
    Page<CourseClass> page(CourseClassQueryParam courseClassQueryParam);

    /**
     * 新增班级
     */
    void addCourseClass(CourseClass courseClass);

    /**
     * 根据ID查询班级
     */
    CourseClass getCourseClassById(Integer id);

    /**
     * 修改班级
     */
    void updateCourseClass(CourseClass courseClass);

    /**
     * 删除班级
     */
    void deleteCourseClass(Integer id);

    /**
     * 统计该班级下的学员人数(删除班级前做校验)
     */
    int countStudentByClassId(Integer id);

    /**
     * 查询所有班级(下拉框)
     */
    List<CourseClass> findAll();

    /**
     * 统计各班级的学员人数
     */
    List<Map<String, Object>> getCourseClassStudentCountData();

    /**
     * 统计学员学历分布
     */
    List<Map<String, Integer>> getStudentDegreeData();

}
