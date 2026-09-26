package com.qiming.controller;

import com.qiming.annotation.RequiresPermission;

import com.qiming.pojo.CourseClassStudentCount;
import com.qiming.pojo.EmployeePositionCount;
import com.qiming.pojo.Result;
import com.qiming.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 查询员工职位分布报表
     */
    @GetMapping("/employee-positions")
    @RequiresPermission("report:employee")
    public Result getEmployeePositionData() {
        log.info("查询员工部门数据报表");
        EmployeePositionCount jobOption = reportService.getEmployeePositionData();
        return Result.success(jobOption);
    }

    /**
     * 查询员工性别分布报表
     */
    @GetMapping("/employee-genders")
    @RequiresPermission("report:employee")
    public Result getEmployeeGenderData() {
        log.info("查询员工性别数据报表");
        List<Map> genderList = reportService.getEmployeeGenderData();
        return Result.success(genderList);
    }

    /**
     * 查询学员人数报表
     */
    @GetMapping("/course-class-student-counts")
    @RequiresPermission("report:student")
    public Result getCourseClassStudentCountData() {
        log.info("查询学员人数报表");
        CourseClassStudentCount clazzOption = reportService.getCourseClassStudentCountData();
        return Result.success(clazzOption);
    }

    /**
     * 查询学员学历分布报表
     */
    @GetMapping("/student-degrees")
    @RequiresPermission("report:student")
    public Result getStudentDegreeData() {
        log.info("查询学员学历分布报表");
        List<Map<String, Integer>> degreeList = reportService.getStudentDegreeData();
        return Result.success(degreeList);
    }

}
