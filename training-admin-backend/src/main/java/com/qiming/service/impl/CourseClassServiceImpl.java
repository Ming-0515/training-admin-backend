package com.qiming.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qiming.mapper.CourseClassMapper;
import com.qiming.mapper.EmployeeMapper;
import com.qiming.pojo.CourseClass;
import com.qiming.pojo.CourseClassQueryParam;
import com.qiming.pojo.PageResult;
import com.qiming.service.CourseClassService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseClassServiceImpl implements CourseClassService {

    @Autowired
    private CourseClassMapper courseClassMapper;
    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 分页查询班级列表
     */
    @Override
    public PageResult<CourseClass> page(CourseClassQueryParam courseClassQueryParam) {
        //设置分页参数
        PageHelper.startPage(courseClassQueryParam.getPage(), courseClassQueryParam.getPageSize());

        // 执行查询
        Page<CourseClass> courseClassNames = courseClassMapper.page(courseClassQueryParam);

        // 设置班级状态
        for (CourseClass courseClass : courseClassNames) {
            if (LocalDate.now().isAfter(courseClass.getEndDate())) {
                courseClass.setStatus("已结课");
            } else if (LocalDate.now().isBefore(courseClass.getBeginDate())) {
                courseClass.setStatus("未开班");
            } else {
                courseClass.setStatus("在读中");
            }
        }

        // 封装结果
        return new PageResult<CourseClass>(courseClassNames.getTotal(), courseClassNames.getResult());
    }

    /**
     * 添加班级
     */
    @Override
    public void addCourseClass(CourseClass courseClass) {
        checkCourseClass(courseClass);

        courseClass.setCreateTime(LocalDateTime.now());
        courseClass.setUpdateTime(LocalDateTime.now());
        courseClassMapper.addCourseClass(courseClass);
    }

    /**
     * 根据ID查询班级
     */
    @Override
    public CourseClass getCourseClassById(Integer id) {
        return courseClassMapper.getCourseClassById(id);
    }

    /**
     * 更新班级
     */
    @Override
    public void updateCourseClass(CourseClass courseClass) {
        if (courseClass == null || courseClass.getId() == null) {
            throw new IllegalArgumentException("班级ID不能为空!");
        }
        checkCourseClass(courseClass);

        courseClass.setUpdateTime(LocalDateTime.now());
        courseClassMapper.updateCourseClass(courseClass);
    }

    /**
     * 删除班级
     */
    @Override
    public void deleteCourseClass(Integer id) {
        // 检查班级是否有学生
        if (courseClassMapper.countStudentByClassId(id) > 0) {
            throw new IllegalArgumentException("对不起, 该班级下有学生, 不能直接删除");
        }

        courseClassMapper.deleteCourseClass(id);
    }

    /**
     * 查询所有班级
     */
    @Override
    public List<CourseClass> findAll() {
        return courseClassMapper.findAll();
    }

    /**
     * 班级关键字段校验
     */
    private void checkCourseClass(CourseClass courseClass) {
        if (courseClass == null) {
            throw new IllegalArgumentException("班级信息不能为空!");
        }
        if (!StringUtils.hasText(courseClass.getName())) {
            throw new IllegalArgumentException("班级名称不能为空!");
        }
        if (courseClass.getName().length() > 30) {
            throw new IllegalArgumentException("班级名称长度不能超过30个字符!");
        }
        if (courseClass.getBeginDate() == null) {
            throw new IllegalArgumentException("开课时间不能为空!");
        }
        if (courseClass.getEndDate() == null) {
            throw new IllegalArgumentException("结课时间不能为空!");
        }
        if (!courseClass.getEndDate().isAfter(courseClass.getBeginDate())) {
            throw new IllegalArgumentException("结课时间必须晚于开课时间!");
        }
        if (courseClass.getSubject() == null) {
            throw new IllegalArgumentException("请选择学科!");
        }
        // courseClass.head_teacher_id 没有外键约束, 这里校验班主任是否存在
        if (courseClass.getHeadTeacherId() != null && employeeMapper.getById(courseClass.getHeadTeacherId()) == null) {
            throw new IllegalArgumentException("所选班主任不存在, 请重新选择!");
        }
    }

}
