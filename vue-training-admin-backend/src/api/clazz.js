import request from "@/utils/request";

/**
 * 班级管理 - 接口
 * 后端: CourseClassController, 路径前缀 /course-classes
 * 注意 begin / end 筛选的是「结课时间」区间, 必须同时传才生效
 */

//分页查询班级
export const queryPageApi = (begin, end, name, page, pageSize) =>
  request.get(`/course-classes?begin=${begin}&end=${end}&name=${name}&page=${page}&pageSize=${pageSize}`);

//新增班级
export const addApi = (clazz) => request.post('/course-classes', clazz);

//根据ID查询
export const queryByIdApi = (id) => request.get(`/course-classes/${id}`);

//修改班级
export const updateApi = (clazz) => request.put('/course-classes', clazz);

//删除班级(路径变量)
export const deleteByIdApi = (id) => request.delete(`/course-classes/${id}`);

//班级下拉框(学员表单用), 只返回 id 与 name
export const queryAllApi = () => request.get('/course-classes/list');
