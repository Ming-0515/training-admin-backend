package com.qiming.service;

import com.qiming.pojo.CourseClassStudentCount;
import com.qiming.pojo.EmployeePositionCount;

import java.util.List;
import java.util.Map;

public interface ReportService {

    /**
     * 查询员工部门数据报表
     */
    EmployeePositionCount getEmployeePositionData();

    /**
     * 查询员工性别分布报表
     */
    List<Map> getEmployeeGenderData();

    /**
     * 查询学员人数报表
     */
    CourseClassStudentCount getCourseClassStudentCountData();

    /**
     * 查询学员学历分布报表
     */
    List<Map<String, Integer>> getStudentDegreeData();
}
