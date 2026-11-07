package com.qiming.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qiming.mapper.DepartmentMapper;
import com.qiming.mapper.EmployeeExperienceMapper;
import com.qiming.mapper.EmployeeMapper;
import com.qiming.pojo.*;
import com.qiming.service.EmployeeService;
import com.qiming.service.MenuService;
import com.qiming.service.RoleService;
import com.qiming.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class EmployeeServiceImpl implements EmployeeService {

    /**
     * 新增员工时的默认初始密码。
     * 之前新增员工不传 password, 会以 NULL 落库, 该员工永远无法登录(登录时 NPE/密码校验必然失败)。
     */
    private static final String DEFAULT_PASSWORD = "123456";

    /**
     * 手机号格式
     */
    private static final String PHONE_REGEX = "^1\\d{10}$";

    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private EmployeeExperienceMapper employeeExperienceMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private RoleService roleService;
    @Autowired
    private MenuService menuService;

    @Override
    public PageResult<Employee> page(EmployeeQueryParam employeeQueryParam) {
        // 设置分页参数
        PageHelper.startPage(employeeQueryParam.getPage(), employeeQueryParam.getPageSize());

        // 执行查询
        Page<Employee> employeeList = employeeMapper.list(employeeQueryParam);

        // 封装分页结果
        return new PageResult<Employee>(employeeList.getTotal(), employeeList.getResult());

    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void addEmployee(Employee employee) {
        checkEmployee(employee);

        // 未指定密码时给默认初始密码, 避免密码落库为 NULL 导致该员工无法登录
        if (!StringUtils.hasText(employee.getPassword())) {
            employee.setPassword(DEFAULT_PASSWORD);
        }

        // 保存员工基本信息
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.addEmployee(employee);

        // 保存员工工作经历
        List<EmployeeExperience> experienceList = employee.getExperienceList();
        if (!CollectionUtils.isEmpty(experienceList)) {
            // 设置员工ID
            experienceList.forEach(expr -> {
                expr.setEmployeeId(employee.getId());
            });

            // 保存员工工作经历
            employeeExperienceMapper.addBatch(experienceList);
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void deleteEmployee(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new IllegalArgumentException("请选择要删除的员工!");
        }

        // 班级的 head_teacher_id 没有外键约束, 这里必须自己拦截:
        // 否则删掉正在带班的员工后, 班级列表的班主任会变成空白, 且无法追溯。
        if (employeeMapper.countCourseClassByHeadTeacherIds(ids) > 0) {
            throw new IllegalArgumentException("选中的员工中有正在担任班主任的, 不能删除! 请先在班级管理中更换班主任。");
        }

        // 删除员工基本信息
        employeeMapper.deleteBatch(ids);
        // 删除员工工作经历
        employeeExperienceMapper.deleteBatch(ids);

    }

    @Override
    public Employee getById(Integer id) {
        // 查询员工基本信息
        return employeeMapper.getById(id);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void updateEmployee(Employee employee) {
        if (employee.getId() == null) {
            throw new IllegalArgumentException("员工ID不能为空!");
        }
        checkEmployee(employee);

        // 更新员工基本信息(不包含密码)
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.updateEmployee(employee);

        // 删除旧的员工工作经历
        employeeExperienceMapper.deleteBatch(List.of(employee.getId()));
        // 更新员工工作经历
        List<EmployeeExperience> experiences = employee.getExperienceList();
        if (!CollectionUtils.isEmpty(experiences)) {
            experiences.forEach(expr -> {
                expr.setEmployeeId(employee.getId());
            });
            employeeExperienceMapper.addBatch(experiences);
        }
    }

    /**
     * 查询所有员工
     */
    @Override
    public List<Employee> findAll() {
        return employeeMapper.findAll();
    }

    /**
     * 登录
     */
    @Override
    public LoginInfo login(Employee employee) {
        if (employee == null || !StringUtils.hasText(employee.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空!");
        }
        // 查询员工基本信息
        Employee loginEmployee = employeeMapper.getByUsername(employee.getUsername());
        if (loginEmployee == null) {
            throw new IllegalArgumentException("用户名不存在!");
        }
        // 校验密码
        if (!StringUtils.hasText(loginEmployee.getPassword()) || !loginEmployee.getPassword().equals(employee.getPassword())) {
            throw new IllegalArgumentException("密码错误!");
        }
        // 生成令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", loginEmployee.getId());
        claims.put("username", loginEmployee.getUsername());
        claims.put("name", loginEmployee.getName());
        String jwt = JwtUtils.generateToken(claims);

        // 封装登录信息, 并下发 RBAC 相关数据:
        // 前端把整个 LoginInfo 存进 localStorage, 用 permissions / menus 做路由守卫与动态菜单。
        // 注意: 前端这些判断只是体验优化, 真正的鉴权在服务端 PermissionInterceptor。
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.setId(loginEmployee.getId());
        loginInfo.setUsername(loginEmployee.getUsername());
        loginInfo.setName(loginEmployee.getName());
        loginInfo.setToken(jwt);

        List<Role> roleList = roleService.listRolesByEmployeeId(loginEmployee.getId());
        List<String> roleCodes = new ArrayList<>();
        for (Role role : roleList) {
            roleCodes.add(role.getCode());
        }
        loginInfo.setRoles(roleCodes);

        loginInfo.setPermissions(new ArrayList<>(menuService.listPermissionCodes(loginEmployee.getId())));
        loginInfo.setMenus(menuService.listMenusByEmployeeId(loginEmployee.getId()));
        return loginInfo;
    }

    /**
     * 员工关键字段校验
     */
    private void checkEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("员工信息不能为空!");
        }
        if (!StringUtils.hasText(employee.getUsername())) {
            throw new IllegalArgumentException("用户名不能为空!");
        }
        if (employee.getUsername().length() < 2 || employee.getUsername().length() > 20) {
            throw new IllegalArgumentException("用户名长度应在2到20个字符之间!");
        }
        if (!StringUtils.hasText(employee.getName())) {
            throw new IllegalArgumentException("姓名不能为空!");
        }
        if (employee.getName().length() > 10) {
            throw new IllegalArgumentException("姓名长度不能超过10个字符!");
        }
        if (employee.getGender() == null || (employee.getGender() != 1 && employee.getGender() != 2)) {
            throw new IllegalArgumentException("请选择正确的性别!");
        }
        if (!StringUtils.hasText(employee.getPhone())) {
            throw new IllegalArgumentException("手机号不能为空!");
        }
        if (!employee.getPhone().matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("请输入有效的手机号!");
        }
        // department_id 没有外键约束, 这里校验部门是否真实存在, 避免出现悬空的部门引用
        if (employee.getDepartmentId() != null && departmentMapper.findById(employee.getDepartmentId()) == null) {
            throw new IllegalArgumentException("所选部门不存在, 请重新选择!");
        }
    }

}
