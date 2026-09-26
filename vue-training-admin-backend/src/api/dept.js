import request from "@/utils/request";

//查询全部部门数据
export const queryAllApi = () => request.get('/departments');

//新增
export const addApi = (dept) => request.post('/departments', dept);

//根据ID查询
export const queryByIdApi = (id) => request.get(`/departments/${id}`);

//修改
export const updateApi = (dept) => request.put('/departments', dept);

//删除(注意: 参数名是单数 id)
export const deleteByIdApi = (id) => request.delete(`/departments?id=${id}`);
