package com.qiming.filter;

import com.qiming.interceptor.OperationLogInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * 操作日志的请求/响应缓存过滤器。
 * <p>
 * 拦截器( OperationLogInterceptor )能拿到类名、方法名、耗时, 但拿不到请求体与返回值。
 * 这里把请求和响应包一层缓存包装器, 再交给拦截器读取:
 * <ul>
 *     <li>{@link ContentCachingRequestWrapper} —— 缓存 @RequestBody 的 JSON 内容, 作为"请求参数"</li>
 *     <li>{@link ContentCachingResponseWrapper} —— 缓存响应内容, 作为"返回值"</li>
 * </ul>
 * 注意顺序: 过滤器在 DispatcherServlet 之外, 拦截器在之内,
 * 所以拦截器的 afterCompletion 执行时缓存内容已经写好了。
 * <p>
 * 只对写操作(POST/PUT/DELETE/PATCH)包装, 查询类请求既不记日志也避免把大响应体读进内存。
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class OperationLogFilter extends OncePerRequestFilter {

    /** 缓存包装后的请求, 供拦截器读取请求体 */
    public static final String ATTR_CACHED_REQUEST = OperationLogFilter.class.getName() + ".cachedRequest";
    /** 缓存包装后的响应, 供拦截器读取返回值 */
    public static final String ATTR_CACHED_RESPONSE = OperationLogFilter.class.getName() + ".cachedResponse";

    /**
     * 请求体最多缓存多少字节。
     * 只是为了取日志里的"请求参数"(日志列本身也只有 2000 字符), 不需要缓存完整请求体;
     * 给一个上限可以避免上传大文件时把整个文件读进内存。
     */
    private static final int REQUEST_CACHE_LIMIT = 4096;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!OperationLogInterceptor.isLoggable(request)) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper requestWrapper =
                new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        request.setAttribute(ATTR_CACHED_REQUEST, requestWrapper);
        request.setAttribute(ATTR_CACHED_RESPONSE, responseWrapper);

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 响应体被包装器缓存住了, 必须写回真正的响应, 否则客户端会收到空响应
            responseWrapper.copyBodyToResponse();
        }
    }

}
