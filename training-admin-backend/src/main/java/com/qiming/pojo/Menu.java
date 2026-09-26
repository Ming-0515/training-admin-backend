package com.qiming.pojo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单 / 权限
 * <p>
 * 同时承载两类数据:
 * <ul>
 *     <li>type=1 菜单: 有 path, 用于前端路由与动态菜单</li>
 *     <li>type=2 按钮/接口权限: 只有 permissionCode, 用于后端鉴权</li>
 * </ul>
 * 分组菜单(如"班级学员管理")没有 permissionCode, 只用于前端分组展示。
 */
@Data
public class Menu {

    /** 菜单类型: 菜单 */
    public static final int TYPE_MENU = 1;
    /** 菜单类型: 按钮 / 接口权限 */
    public static final int TYPE_BUTTON = 2;

    private Integer id;                 //ID, 主键
    private Integer parentId;           //父级ID, 0 表示顶级
    private String name;                //名称
    private Integer type;               //类型, 1:菜单, 2:按钮/接口权限
    private String permissionCode;      //权限标识, 如 class:delete
    private String path;                //前端路由, 仅菜单有值
    private Integer sort;               //排序
    private LocalDateTime createTime;   //创建时间
    private LocalDateTime updateTime;   //修改时间

    private List<Menu> children;        //子节点(树形返回时填充)

}
