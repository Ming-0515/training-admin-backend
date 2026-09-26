package com.qiming.exception;

/**
 * 权限不足异常。
 * 由 PermissionInterceptor 在权限校验失败时抛出, 交给 GlobalExceptionHandler 统一处理,
 * 最终返回 HTTP 403 + {"code":0,"msg":"..."}。
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

}
