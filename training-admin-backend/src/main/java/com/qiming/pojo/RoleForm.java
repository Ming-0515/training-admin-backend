package com.qiming.pojo;

import lombok.Data;

import java.util.List;

/**
 * 角色新增/修改表单
 * 角色本身的信息 + 该角色勾选的菜单权限ID
 */
@Data
public class RoleForm {

    private Integer id;                 //ID, 修改时必填
    private String name;                //角色名称
    private String code;                //角色标识
    private String remark;              //备注
    private List<Integer> menuIds;      //该角色拥有的菜单/权限ID

}
