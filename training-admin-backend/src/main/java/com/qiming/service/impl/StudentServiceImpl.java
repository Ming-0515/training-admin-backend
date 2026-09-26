package com.qiming.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qiming.mapper.CourseClassMapper;
import com.qiming.mapper.StudentMapper;
import com.qiming.pojo.PageResult;
import com.qiming.pojo.Student;
import com.qiming.pojo.StudentQueryParam;
import com.qiming.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class StudentServiceImpl implements StudentService {

    /** 手机号格式 */
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    /** 身份证号格式: 18 位数字 */
    private static final String ID_CARD_REGEX = "^\\d{18}$";

    @Autowired
    private StudentMapper studentMapper;
    @Autowired
    private CourseClassMapper courseClassMapper;

    /**
     * 查询学员分页数据
     */
    @Override
    public PageResult<Student> page(StudentQueryParam studentQueryParam) {
        // 设置分页参数
        PageHelper.startPage(studentQueryParam.getPage(),studentQueryParam.getPageSize());
        // 执行查询
        Page<Student> studentList = studentMapper.page(studentQueryParam);
        // 封装分页结果
        return new PageResult<Student>(studentList.getTotal(), studentList.getResult());
    }

    /**
     * 添加学员
     */
    @Override
    public void addStudent(Student student) {
        checkStudent(student);

        student.setCreateTime(LocalDateTime.now());
        student.setUpdateTime(LocalDateTime.now());
        student.setViolationCount((short) 0);
        student.setViolationScore((short) 0);

        studentMapper.addStudent(student);
    }

    /**
     * 根据id查询学员详情
     */
    @Override
    public Student getStudentById(Integer id) {
        return studentMapper.getStudentById(id);
    }

    /**
     * 更新学员信息
     */
    @Override
    public void updateStudent(Student student) {
        if (student == null || student.getId() == null) {
            throw new IllegalArgumentException("学员ID不能为空!");
        }
        checkStudent(student);

        student.setUpdateTime(LocalDateTime.now());
        studentMapper.updateStudent(student);
    }

    /**
     * 删除学员
     */
    @Override
    public void deleteStudentById(Integer[] ids) {
        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException("请选择要删除的学员!");
        }
        studentMapper.deleteStudentById(ids);
    }

    /**
     * 更新学员违纪扣分
     */
    @Override
    public void updateStudentViolation(Integer id, Short score) {
        // 先查找出该学生
        Student student = studentMapper.getStudentById(id);
        // 违纪次数加一
        student.setViolationCount((short) (student.getViolationCount() + 1));
        // 违纪扣分加
        student.setViolationScore((short) (student.getViolationScore() + score));
        // 更新数据库
        studentMapper.updateStudent(student);
    }

    /**
     * 学员关键字段校验。
     * 原来后端完全没有校验, 最典型的问题是: 不选班级直接提交时 class_id 为 NULL,
     * 而该列是 NOT NULL, 用户只会看到一个 500 "程序出错了!"。
     */
    private void checkStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("学员信息不能为空!");
        }
        if (!StringUtils.hasText(student.getName())) {
            throw new IllegalArgumentException("姓名不能为空!");
        }
        if (student.getName().length() > 10) {
            throw new IllegalArgumentException("姓名长度不能超过10个字符!");
        }
        if (!StringUtils.hasText(student.getStudentNo())) {
            throw new IllegalArgumentException("学号不能为空!");
        }
        if (student.getStudentNo().length() != 10) {
            throw new IllegalArgumentException("学号长度必须为10个字符!");
        }
        if (student.getGender() == null || (student.getGender() != 1 && student.getGender() != 2)) {
            throw new IllegalArgumentException("请选择正确的性别!");
        }
        if (!StringUtils.hasText(student.getPhone())) {
            throw new IllegalArgumentException("手机号不能为空!");
        }
        if (!student.getPhone().matches(PHONE_REGEX)) {
            throw new IllegalArgumentException("请输入合法的手机号!");
        }
        if (!StringUtils.hasText(student.getIdCard())) {
            throw new IllegalArgumentException("身份证号不能为空!");
        }
        if (!student.getIdCard().matches(ID_CARD_REGEX)) {
            throw new IllegalArgumentException("请输入合法的身份证号!");
        }
        if (student.getIsCollege() == null) {
            throw new IllegalArgumentException("请选择是否为院校学员!");
        }
        if (student.getClassId() == null) {
            throw new IllegalArgumentException("请选择所属班级!");
        }
        // student.class_id 没有外键约束, 这里校验班级是否存在, 避免出现悬空的班级引用
        if (courseClassMapper.getCourseClassById(student.getClassId()) == null) {
            throw new IllegalArgumentException("所选班级不存在, 请重新选择!");
        }
    }

}
