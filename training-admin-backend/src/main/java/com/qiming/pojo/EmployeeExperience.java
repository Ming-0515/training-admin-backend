package com.qiming.pojo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 员工工作经历
 */
@Data
public class EmployeeExperience {

    private Integer id;             //ID, 主键
    private Integer employeeId;     //员工ID
    private LocalDate beginDate;    //开始时间
    private LocalDate endDate;      //结束时间
    private String company;         //公司名称
    private String position;        //职位

}
