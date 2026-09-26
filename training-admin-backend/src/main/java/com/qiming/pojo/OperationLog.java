package com.qiming.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志
 * 对应前端「数据统计管理 -> 日志信息统计」页面的表格字段
 */
@Data
public class OperationLog {

    private Integer id;                 //ID, 主键
    private Integer operatorId;       //操作人ID
    private String operatorName;      //操作人姓名
    private LocalDateTime operationTime;  //操作时间
    private String className;           //类名
    private String methodName;          //方法名
    private String methodParams;        //请求参数(password 已掩码)
    private String returnValue;         //返回值
    private Long costTime;              //操作耗时(ms)
    private String operationIp;           //操作IP

}
