package com.qiming.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 各班学员人数报表数据。
 * 两个 List 按下标一一对应, 供前端 ECharts 直接使用:
 * courseClassNames = 班级名称列表, counts = 对应学员人数列表。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseClassStudentCount {

    private List<Object> courseClassNames;
    private List<Object> counts;

}
