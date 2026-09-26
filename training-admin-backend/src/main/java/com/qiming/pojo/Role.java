package com.qiming.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色
 */
@Data
public class Role {

    private Integer id;                 //ID, 主键
    private String name;                //角色名称
    private String code;                //角色标识, 如 ADMIN
    private String remark;              //备注
    private LocalDateTime createTime;   //创建时间
    private LocalDateTime updateTime;   //修改时间

}
