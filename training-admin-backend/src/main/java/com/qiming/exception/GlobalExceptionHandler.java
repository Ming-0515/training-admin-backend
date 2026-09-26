package com.qiming.exception;

import com.qiming.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 权限不足。
     * 返回 HTTP 403(前端拦截器据此提示"无权限"), 响应体仍然是统一的 Result 结构。
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleForbiddenException(ForbiddenException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /**
     * 请求体不是合法 JSON。
     * 不处理的话会落到下面的兜底分支, 客户端只能看到"程序出错了!", 完全定位不到原因。
     */
    @ExceptionHandler
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.error("请求体格式错误, 请检查 JSON 是否合法!");
    }

    /**
     * 缺少必填的请求参数(如批量删除未传 ids)。
     */
    @ExceptionHandler
    public Result handleMissingParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return Result.error("缺少必填参数: " + e.getParameterName() + "!");
    }

    @ExceptionHandler
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("重复键异常", e);
        // 注意: 不要直接按空格切割数据库错误信息(下标容易越界),
        // 否则异常处理器自己会再抛一个异常, 反而把真实原因掩盖掉。
        String value = null;
        String message = e.getMessage();
        int index = message == null ? -1 : message.indexOf("Duplicate entry");
        if (index >= 0) {
            String[] arr = message.substring(index).split(" ");
            if (arr.length > 2) {
                value = arr[2];
            }
        }
        return Result.error(value == null ? "数据已存在, 请检查后重试!" : value + "已存在!");
    }

    @ExceptionHandler
    public Result handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    @ExceptionHandler
    public Result handleException(Exception e) {
        log.error("程序出错了!", e);
        return Result.error("程序出错了!");
    }

}
