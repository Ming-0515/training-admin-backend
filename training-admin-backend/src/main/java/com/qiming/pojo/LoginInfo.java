package com.qiming.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录返回信息。
 * <p>
 * 除令牌外还下发角色、权限标识与可见菜单, 前端会把整个对象存进 localStorage,
 * 用于动态菜单与路由守卫(仅为体验优化, 真正的鉴权在服务端 PermissionInterceptor)。
 */
@Data
@NoArgsConstructor
public class LoginInfo {

    private Integer id;                     //员工ID
    private String username;                //用户名
    private String name;                    //姓名
    private String token;                   //令牌
    private List<String> roles;             //角色标识集合, 如 [ADMIN]
    private List<String> permissions;       //权限标识集合, 如 [class:list, class:delete]
    private List<Menu> menus;               //可见菜单(带前端路由)

}
