package com.qiming.interceptor;

import com.qiming.utils.JwtUtils;
import com.qiming.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录鉴权拦截器(只负责"有没有登录、令牌是否有效")。
 * 通过后把当前登录人写入 {@link UserContext}, 供后续的权限校验与业务代码使用。
 */
@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 防御性清理: 线程池复用下, 避免上一个请求留下的用户信息泄漏到本次请求
        UserContext.clear();

        // 从请求头中获取token
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            log.info("请求头中没有token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 校验token是否有效
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token);
        } catch (Exception e) {
            log.error("token校验失败");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 写入当前登录人上下文
        Object id = claims.get("id");
        Integer employeeId = id instanceof Number number ? number.intValue() : null;
        UserContext.set(employeeId, claims.get("username", String.class), claims.get("name", String.class));

        log.info("token校验通过");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束务必清理 ThreadLocal
        UserContext.clear();
    }

}
