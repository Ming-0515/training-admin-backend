package com.qiming.mapper;

import com.github.pagehelper.Page;
import com.qiming.pojo.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper {

    /**
     * 插入一条操作日志
     */
    void insert(OperationLog operationLog);

    /**
     * 分页查询操作日志
     */
    Page<OperationLog> page();

}
