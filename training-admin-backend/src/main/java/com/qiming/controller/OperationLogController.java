package com.qiming.controller;

import com.qiming.annotation.RequiresPermission;

import com.qiming.pojo.PageResult;
import com.qiming.pojo.OperationLog;
import com.qiming.pojo.Result;
import com.qiming.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志
 * 前端「数据统计管理 -> 日志信息统计」页面调用 GET /log/page?page=&pageSize=
 */
@Slf4j
@RestController
@RequestMapping("/operation-logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    @RequiresPermission("report:log")
    public Result page(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("分页查询操作日志: page={}, pageSize={}", page, pageSize);
        PageResult<OperationLog> pageResult = operationLogService.page(page, pageSize);
        return Result.success(pageResult);
    }

}
