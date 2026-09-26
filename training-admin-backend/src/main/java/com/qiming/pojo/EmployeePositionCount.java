package com.qiming.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 员工职位分布报表数据。
 * 两个 List 按下标一一对应, 供前端 ECharts 直接使用:
 * positions = 职位名称列表, counts = 对应人数列表。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeePositionCount {

    private List<Object> positions;
    private List<Object> counts;

}
