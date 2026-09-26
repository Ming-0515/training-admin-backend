package com.qiming.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工
 */
@Data
public class Employee {

    private Integer id;                     //ID, 主键
    private String username;                //登录用户名

    /**
     * 登录密码。只允许客户端提交(登录/新增时), 绝不允许序列化返回给前端, 防止密码泄露。
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;                //登录密码

    private String name;                    //姓名
    private Integer gender;                 //性别, 1:男, 2:女
    private String phone;                   //手机号
    private Integer position;               //职位, 1:班主任, 2:讲师, 3:学工主管, 4:教研主管, 5:咨询师, 6:其他
    private Integer salary;                 //薪资
    private String avatar;                  //头像访问地址, 由本地文件服务返回
    private LocalDate entryDate;            //入职日期
    private Integer departmentId;           //所属部门ID
    private LocalDateTime createTime;       //创建时间
    private LocalDateTime updateTime;       //修改时间

    private String departmentName;          //所属部门名称(关联查询结果)
    private List<EmployeeExperience> experienceList;  //工作经历列表

}
