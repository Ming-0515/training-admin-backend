import request from "@/utils/request";

/**
 * 报表统计 - 接口
 * 后端: ReportController, 路径前缀 /reports
 * 四个接口都无入参、不分页, 只给 ECharts 用
 */

//员工职位分布 -> { positions: [...], counts: [...] }
export const queryEmpPositionApi = () => request.get('/reports/employee-positions');

//员工性别分布 -> [{ name: '男', value: 4 }, ...]
export const queryEmpGenderApi = () => request.get('/reports/employee-genders');

//各班级学员人数 -> { courseClassNames: [...], counts: [...] }
export const queryClassStudentCountApi = () => request.get('/reports/course-class-student-counts');

//学员学历分布 -> [{ name: '本科', value: 4 }, ...]
export const queryStudentDegreeApi = () => request.get('/reports/student-degrees');
