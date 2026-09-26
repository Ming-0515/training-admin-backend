package com.qiming.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qiming.mapper.OperationLogMapper;
import com.qiming.pojo.OperationLog;
import com.qiming.pojo.PageResult;
import com.qiming.service.OperationLogService;
import com.qiming.utils.PageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * 保存操作日志。
     * 用 REQUIRES_NEW 开启独立事务: 业务方法如果抛异常回滚了, 日志仍然要落库,
     * 否则"操作失败"这件事反而记不下来, 也避免日志插入失败把业务事务标记成回滚。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void save(OperationLog operationLog) {
        operationLogMapper.insert(operationLog);
    }

    /**
     * 分页查询操作日志
     */
    @Override
    public PageResult<OperationLog> page(Integer page, Integer pageSize) {
        PageHelper.startPage(PageUtils.normalizePage(page), PageUtils.normalizePageSize(pageSize));
        Page<OperationLog> logList = operationLogMapper.page();
        return new PageResult<OperationLog>(logList.getTotal(), logList.getResult());
    }

}
