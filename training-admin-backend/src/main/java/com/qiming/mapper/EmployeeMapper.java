package com.qiming.mapper;

import com.github.pagehelper.Page;
import com.qiming.pojo.Employee;
import com.qiming.pojo.EmployeeQueryParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EmployeeMapper {

    /**
     * 分页查询员工列表
     */
    Page<Employee> list(EmployeeQueryParam employeeQueryParam);

    /**
     * 添加员工。
     * 注意: 自增主键回填写在 EmployeeMapper.xml 的 {@code <insert useGeneratedKeys="true" keyProperty="id">} 上。
     * 语句由 XML 定义时, 接口方法上的 @Options 不会生效, 只写注解会导致拿不到自增主键。
     */
    void addEmployee(Employee employee);

    /**
     * 批量删除员工基本信息
     */
    void deleteBatch(List<Integer> ids);

    /**
     * 根据id查找员工(含工作经历)
     */
    Employee getById(Integer id);

    /**
     * 更新员工基本信息(不包含密码)
     */
    void updateEmployee(Employee employee);

    /**
     * 查询员工职位分布
     */
    List<Map<String, Object>> getEmployeePositionData();

    /**
     * 查询员工性别分布
     */
    List<Map> getEmployeeGenderData();

    /**
     * 查询所有员工(用于班级的班主任下拉框)。
     * 下拉框只需要 id + name, 刻意不查 salary / phone 等字段,
     * 避免把整个员工表暴露给只需要"选一个人"的场景。
     */
    @Select("select id, name from employee")
    List<Employee> findAll();

    /**
     * 查询部门下员工人数
     */
    int countByDepartmentId(Integer id);

    /**
     * 查询选中员工中有多少人正在担任班级班主任
     */
    int countCourseClassByHeadTeacherIds(List<Integer> ids);

    /**
     * 根据用户名查询员工(登录用, 唯一需要读取 password 的地方)
     */
    Employee getByUsername(String username);

}
