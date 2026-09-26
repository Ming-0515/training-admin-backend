import request from "@/utils/request";

/**
 * 操作日志 - 接口
 * 后端: OperationLogController, 路径前缀 /operation-logs
 */

//分页查询操作日志(权限: report:log)
export const queryPageApi = (page, pageSize) =>
  request.get(`/operation-logs/page?page=${page}&pageSize=${pageSize}`);
