package com.qiming.controller;

import com.qiming.annotation.RequiresPermission;

import com.qiming.pojo.PageResult;
import com.qiming.pojo.Result;
import com.qiming.pojo.Student;
import com.qiming.pojo.StudentQueryParam;
import com.qiming.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    /**
     * 查询学员分页数据
     */
    @GetMapping
    @RequiresPermission("student:list")
    public Result page(StudentQueryParam studentQueryParam) {
        log.info("查询学员分页数据");
        PageResult<Student> studentList = studentService.page(studentQueryParam);
        return Result.success(studentList);
    }

    /**
     * 添加学员
     */
    @PostMapping
    @RequiresPermission("student:create")
    public Result addStudent(@RequestBody Student student) {
        log.info("添加学员: {}", student);
        studentService.addStudent(student);
        return Result.success();
    }

    /**
     * 根据id查询学员详情
     */
    @GetMapping("/{id}")
    @RequiresPermission("student:list")
    public Result getStudentById(@PathVariable Integer id) {
        log.info("根据id查询学员详情: {}", id);
        Student student = studentService.getStudentById(id);
        return Result.success(student);
    }

    /**
     * 更新学员信息
     */
    @PutMapping
    @RequiresPermission("student:update")
    public Result updateStudent(@RequestBody Student student) {
        log.info("更新学员信息: {}", student);
        studentService.updateStudent(student);
        return Result.success();
    }

    @DeleteMapping("/{ids}")
    @RequiresPermission("student:delete")
    public Result deleteStudentById(@PathVariable Integer[] ids) {
        log.info("删除学员: {}", (Object[]) ids);
        studentService.deleteStudentById(ids);
        return Result.success();
    }

    /**
     * 更新学员违纪扣分
     */
    @PutMapping("/violations/{id}/{score}")
    @RequiresPermission("student:violation")
    public Result updateStudentViolation(@PathVariable Integer id, @PathVariable Short score) {
        log.info("更新学员{}违纪扣分:扣{}分", id, score);
        studentService.updateStudentViolation(id, score);
        return Result.success();
    }

}
