package com.qiming.interceptor;

import com.qiming.filter.OperationLogFilter;
import com.qiming.pojo.OperationLog;
import com.qiming.service.OperationLogService;
import com.qiming.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 操作日志拦截器。
 * <p>
 * 只记录写操作(POST/PUT/DELETE/PATCH), 查询接口不记 —— 否则每翻一次页、每刷一次报表
 * 都会往日志表里插一行, 日志表很快就会失去意义。
 * <p>
 * 类名与方法名来自 HandlerMethod, 请求体/返回值来自 {@link OperationLogFilter} 的缓存包装器。
 * 注册顺序必须排在 TokenInterceptor 之后, 这样未通过鉴权的请求不会产生日志。
 */
@Slf4j
@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    /** 需要记录日志的请求方法 */
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "DELETE", "PATCH");

    /** 日志字段最大长度, 与 operation_log 表的列长度保持一致 */
    private static final int MAX_CONTENT_LENGTH = 2000;

    /** 密码掩码: 请求体里出现 "password":"xxx" 时替换为 **** */
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("(\"[Pp]assword\"\\s*:\\s*\")[^\"]*(\")");

    /** 登录请求体里的用户名, 用于登录失败(拿不到 token)时也能记录操作人 */
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("\"username\"\\s*:\\s*\"([^\"]*)\"");

    /**
     * 从统一返回结构 {"code":..,"data":..,"msg":".."} 里只取 code 与 msg。
     * 不允许把整个响应体写进日志: 一来日志表会被撑爆,
     * 二来登录响应体里带 token, 整段落库等于把可用令牌泄漏给所有能看日志的人。
     */
    private static final Pattern RESULT_CODE_PATTERN =
            Pattern.compile("^\\s*\\{\\s*\"code\"\\s*:\\s*(-?\\d+)");
    private static final Pattern RESULT_MSG_PATTERN =
            Pattern.compile("\"msg\"\\s*:\\s*\"([^\"]*)\"");

    private static final String ATTR_LOGGABLE = OperationLogInterceptor.class.getName() + ".loggable";
    private static final String ATTR_START_TIME = OperationLogInterceptor.class.getName() + ".startTime";
    private static final String ATTR_CLASS_NAME = OperationLogInterceptor.class.getName() + ".className";
    private static final String ATTR_METHOD_NAME = OperationLogInterceptor.class.getName() + ".methodName";

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 是否是写操作(需要记录日志)
     */
    public static boolean isLoggable(HttpServletRequest request) {
        String method = request.getMethod();
        return method != null && WRITE_METHODS.contains(method.toUpperCase());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!isLoggable(request) || !(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        request.setAttribute(ATTR_LOGGABLE, Boolean.TRUE);
        request.setAttribute(ATTR_START_TIME, System.currentTimeMillis());
        request.setAttribute(ATTR_CLASS_NAME, handlerMethod.getBeanType().getSimpleName());
        request.setAttribute(ATTR_METHOD_NAME, handlerMethod.getMethod().getName());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!Boolean.TRUE.equals(request.getAttribute(ATTR_LOGGABLE))) {
            return;
        }
        // 记录日志属于辅助功能, 任何异常都不能影响正常业务响应
        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setOperationTime(LocalDateTime.now());
            operationLog.setClassName((String) request.getAttribute(ATTR_CLASS_NAME));
            operationLog.setMethodName((String) request.getAttribute(ATTR_METHOD_NAME));
            operationLog.setMethodParams(resolveParams(request));
            operationLog.setReturnValue(resolveResult(request, response));
            operationLog.setOperationIp(resolveIp(request));

            Object startTime = request.getAttribute(ATTR_START_TIME);
            operationLog.setCostTime(startTime instanceof Long s ? System.currentTimeMillis() - s : 0L);

            resolveOperator(request, operationLog);

            operationLogService.save(operationLog);
        } catch (Exception e) {
            log.error("记录操作日志失败", e);
        }
    }

    /**
     * 请求参数: 优先取缓存的请求体(JSON), 没有请求体时退化为查询字符串
     */
    private String resolveParams(HttpServletRequest request) {
        String body = rawRequestBody(request);
        if (!StringUtils.hasText(body)) {
            body = request.getQueryString();
        }
        return truncate(maskPassword(body));
    }

    /**
     * 缓存的原始请求体(可能被过滤器按上限截断)
     */
    private String rawRequestBody(HttpServletRequest request) {
        Object cached = request.getAttribute(OperationLogFilter.ATTR_CACHED_REQUEST);
        if (cached instanceof ContentCachingRequestWrapper wrapper) {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    /**
     * 操作结果: 只记录一个简短的结论, 不记录完整响应体。
     * <p>
     * 通用接口返回 {"code":1|0,"msg":"..."}, 因此只从中提取 code 与 msg:
     * <ul>
     *     <li>成功 -&gt; "成功"</li>
     *     <li>业务失败 -&gt; "失败: 具体原因"(如 "失败: 密码错误!")</li>
     *     <li>没有响应体(如未登录被拦) -&gt; 按 HTTP 状态码给出结论</li>
     * </ul>
     * 这样既保留了"这次操作成没成、为什么没成"这个日志真正要回答的问题,
     * 又不会把响应体(可能含 token 等敏感数据)整段写进日志表。
     */
    private String resolveResult(HttpServletRequest request, HttpServletResponse response) {
        String body = cachedResponseBody(request);
        if (StringUtils.hasText(body)) {
            Matcher codeMatcher = RESULT_CODE_PATTERN.matcher(body);
            if (codeMatcher.find()) {
                if ("1".equals(codeMatcher.group(1))) {
                    return "成功";
                }
                Matcher msgMatcher = RESULT_MSG_PATTERN.matcher(body);
                String msg = msgMatcher.find() ? msgMatcher.group(1) : "";
                return StringUtils.hasText(msg) ? truncate("失败: " + msg) : "失败";
            }
        }
        int status = response == null ? 200 : response.getStatus();
        return (status >= 200 && status < 300) ? "成功" : "失败: HTTP " + status;
    }

    /**
     * 缓存的原始响应体(仅用于上面提取 code/msg, 不会整段落库)
     */
    private String cachedResponseBody(HttpServletRequest request) {
        Object cached = request.getAttribute(OperationLogFilter.ATTR_CACHED_RESPONSE);
        if (cached instanceof ContentCachingResponseWrapper wrapper) {
            byte[] content = wrapper.getContentAsByteArray();
            if (content.length > 0) {
                return new String(content, StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    /**
     * 操作人: 优先从 token 中解析。
     * 登录请求此时还没有 token, 退化为取请求体里的 username ——
     * 这样"登录失败"也能留下是谁在尝试登录, 这正是登录审计最需要的信息。
     */
    private void resolveOperator(HttpServletRequest request, OperationLog operationLog) {
        String token = request.getHeader("token");
        if (StringUtils.hasText(token)) {
            try {
                Claims claims = JwtUtils.parseToken(token);
                Object id = claims.get("id");
                if (id instanceof Number number) {
                    operationLog.setOperatorId(number.intValue());
                }
                // 老 token 的 claims 里没有 name, 退化为用户名, 保证这一列不会为空
                String name = claims.get("name", String.class);
                String username = claims.get("username", String.class);
                operationLog.setOperatorName(StringUtils.hasText(name) ? name : username);
                return;
            } catch (Exception e) {
                log.warn("操作日志解析token失败: {}", e.getMessage());
            }
        }
        // 兜底: 从请求体里取 username(登录场景)
        Matcher matcher = USERNAME_PATTERN.matcher(rawRequestBody(request));
        operationLog.setOperatorName(matcher.find() ? truncate(matcher.group(1)) : "");
    }

    /**
     * 操作IP。注意: 目前请求经 nginx 代理, 未转发真实IP时这里拿到的是 127.0.0.1
     */
    private String resolveIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip)) {
            // X-Forwarded-For 可能是 "客户端IP, 代理1, 代理2"
            return truncate(ip.split(",")[0].trim());
        }
        return truncate(request.getRemoteAddr());
    }

    /**
     * 掩码密码。请求体是原始 JSON 字符串, 不经过 Jackson 的 WRITE_ONLY 处理,
     * 所以必须在这里把 password 换成 ****, 避免明文密码落进日志表。
     */
    private String maskPassword(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        return PASSWORD_PATTERN.matcher(content).replaceAll("$1****$2");
    }

    private String truncate(String content) {
        if (content == null) {
            return "";
        }
        if (content.length() <= MAX_CONTENT_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_CONTENT_LENGTH);
    }

}
