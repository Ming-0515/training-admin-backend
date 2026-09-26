package com.qiming.service.impl;

import com.qiming.mapper.CourseClassMapper;
import com.qiming.mapper.EmployeeMapper;
import com.qiming.pojo.CourseClassStudentCount;
import com.qiming.pojo.EmployeePositionCount;
import com.qiming.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private CourseClassMapper courseClassMapper;

    @Override
    public EmployeePositionCount getEmployeePositionData() {
        List<Map<String, Object>> list = employeeMapper.getEmployeePositionData();
        // 键名必须与 EmployeeMapper.xml 中 getEmployeePositionData 的列别名一致
        List<Object> positions = list.stream().map(item -> item.get("positionName")).toList();
        List<Object> counts = list.stream().map(item -> item.get("employeeCount")).toList();
        return new EmployeePositionCount(positions, counts);
    }

    @Override
    public List<Map> getEmployeeGenderData() {
        return employeeMapper.getEmployeeGenderData();
    }

    /**
     * 查询学员人数报表
     */
    @Override
    public CourseClassStudentCount getCourseClassStudentCountData() {
        List<Map<String, Object>> list = courseClassMapper.getCourseClassStudentCountData();
        List<Object> courseClassNames = list.stream().map(item -> item.get("courseClassName")).toList();
        List<Object> counts = list.stream().map(item -> item.get("studentCount")).toList();
        return new CourseClassStudentCount(courseClassNames, counts);
    }

    /**
     * 查询学员学历分布报表
     */
    @Override
    public List<Map<String, Integer>> getStudentDegreeData() {
        return courseClassMapper.getStudentDegreeData();
    }

}
