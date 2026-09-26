package com.qiming.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 班级
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseClass {

    private Integer id;                     //ID, 主键
    private String name;                    //班级名称
    private String room;                    //班级教室
    private LocalDate beginDate;            //开课时间
    private LocalDate endDate;              //结课时间
    private Integer headTeacherId;          //班主任ID
    private Integer subject;                //学科, 1:Java, 2:前端, 3:大数据, 4:Python, 5:Go, 6:嵌入式
    private LocalDateTime createTime;       //创建时间
    private LocalDateTime updateTime;       //修改时间

    private String headTeacherName;         //班主任姓名(关联查询结果)
    private String status;                  //班级状态: 未开班 / 在读中 / 已结课(按日期计算, 不入库)

}
