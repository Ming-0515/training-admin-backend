package com.qiming.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限校验注解。
 * <p>
 * 标在 Controller 方法(或类)上, 由 {@code PermissionInterceptor} 在进入方法前校验:
 * 当前登录用户只要拥有 value 中<b>任意一个</b>权限即可放行。
 * <p>
 * 示例:
 * <pre>
 * &#64;RequiresPermission("employee:delete")
 * public Result deleteEmployee(...) { ... }
 *
 * // 满足其一即可
 * &#64;RequiresPermission({"employee:create", "employee:update"})
 * public Result upload(...) { ... }
 * </pre>
 * 拥有 ADMIN 角色的用户默认放行全部接口。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /** 需要的权限标识, 满足任意一个即可 */
    String[] value();

}
