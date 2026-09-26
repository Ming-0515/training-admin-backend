import request from "@/utils/request";

/**
 * 学员管理 - 接口
 * 后端: StudentController, 路径前缀 /students
 */

//分页查询学员
export const queryPageApi = (classId, degree, name, page, pageSize) =>
  request.get(`/students?classId=${classId}&degree=${degree}&name=${name}&page=${page}&pageSize=${pageSize}`);

//新增学员
export const addApi = (stu) => request.post('/students', stu);

//根据ID查询
export const queryByIdApi = (id) => request.get(`/students/${id}`);

//修改学员(全量更新, violationCount / violationScore 也要一并回传)
export const updateApi = (stu) => request.put('/students', stu);

//批量删除学员(注意: 是路径变量, 多个id用逗号分隔, 与员工的 ?ids= 不同)
export const deleteByIdApi = (ids) => request.delete(`/students/${ids}`);

//学员违纪扣分(score 是本次扣分, 服务端做累加)
export const violationApi = (id, score) => request.put(`/students/violations/${id}/${score}`);
