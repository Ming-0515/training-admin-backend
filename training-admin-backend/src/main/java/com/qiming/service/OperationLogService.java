package com.qiming.service;

import com.qiming.pojo.OperationLog;
import com.qiming.pojo.PageResult;

public interface OperationLogService {

    /**
     * 保存一条操作日志
     */
    void save(OperationLog operationLog);

    /**
     * 分页查询操作日志
     */
    PageResult<OperationLog> page(Integer page, Integer pageSize);

}
