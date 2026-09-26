package com.qiming.interceptor;

import com.qiming.annotation.RequiresPermission;
import com.qiming.exception.ForbiddenException;
import com.qiming.service.MenuService;
import com.qiming.service.RoleService;
import com.qiming.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 接口权限校验拦截器(RBAC 的落地位置)。
 * <p>
 * 读取 Controller 方法(或类)上的 {@link RequiresPermission}, 与当前登录用户
 * 通过「员工 -> 角色 -> 菜单」链路拿到的权限标识做比对:
 * <ul>
 *     <li>拥有 ADMIN 角色的用户直接放行</li>
 *     <li>value 中任意一个权限命中即放行</li>
 *     <li>否则抛出 ForbiddenException, 由全局异常处理器返回 403</li>
 * </ul>
 * 注册顺序必须排在 TokenInterceptor 之后(否则拿不到当前登录人),
 * 但排在 OperationLogInterceptor 之后, 这样"越权被拒"这件事本身也会留下日志。
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private MenuService menuService;
    @Autowired
    private RoleService roleService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 方法上的注解优先, 其次取类上的
        RequiresPermission required = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (required == null) {
            required = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
        }
        if (required == null) {
            return true;
        }

        Integer employeeId = UserContext.getEmployeeId();
        if (employeeId == null) {
            throw new ForbiddenException("未登录或登录状态已失效, 请重新登录!");
        }

        // 超级管理员放行: 新增权限时无需再回头给 ADMIN 补授权
        if (roleService.isAdmin(employeeId)) {
            return true;
        }

        Set<String> owned = menuService.listPermissionCodes(employeeId);
        for (String code : required.value()) {
            if (owned.contains(code)) {
                return true;
            }
        }

        log.warn("权限校验未通过: 用户={}, 接口={}.{}, 需要权限={}, 实际拥有={}",
                UserContext.getUsername(), handlerMethod.getBeanType().getSimpleName(),
                handlerMethod.getMethod().getName(), String.join("|", required.value()), owned);
        throw new ForbiddenException("权限不足: 需要【" + String.join(" 或 ", required.value()) + "】权限, 请联系管理员!");
    }

}
