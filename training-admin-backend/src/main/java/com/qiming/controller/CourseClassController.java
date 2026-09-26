package com.qiming.controller;

import com.qiming.annotation.RequiresPermission;

import com.qiming.pojo.CourseClass;
import com.qiming.pojo.CourseClassQueryParam;
import com.qiming.pojo.PageResult;
import com.qiming.pojo.Result;
import com.qiming.service.CourseClassService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/course-classes")
@Slf4j
public class CourseClassController {

    @Autowired
    private CourseClassService courseClassService;

    /**
     * 分页查询班级列表
     */
    @GetMapping
    @RequiresPermission("class:list")
    public Result page(CourseClassQueryParam courseClassQueryParam){
        log.info("分页查询班级列表:{}",courseClassQueryParam);
        PageResult<CourseClass> pageResult = courseClassService.page(courseClassQueryParam);
        return Result.success(pageResult);
    }

    /**
     * 添加班级
     */
    @PostMapping
    @RequiresPermission("class:create")
    public Result addCourseClass(@RequestBody CourseClass courseClass){
        log.info("增加班级:{}",courseClass);
        courseClassService.addCourseClass(courseClass);
        return Result.success();
    }

    /**
     * 根据ID查询班级
     */
    @GetMapping("/{id}")
    @RequiresPermission("class:list")
    public Result getCourseClassById(@PathVariable Integer id){
        log.info("根据ID查询班级:{}",id);
        CourseClass courseClass = courseClassService.getCourseClassById(id);
        return Result.success(courseClass);
    }

    /**
     * 更新班级
     */
    @PutMapping
    @RequiresPermission("class:update")
    public Result updateCourseClass(@RequestBody CourseClass courseClass){
        log.info("更新班级:{}",courseClass);
        courseClassService.updateCourseClass(courseClass);
        return Result.success();
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/{id}")
    @RequiresPermission("class:delete")
    public Result deleteCourseClass(@PathVariable Integer id){
        log.info("删除班级:{}",id);
        courseClassService.deleteCourseClass(id);
        return Result.success();
    }

    /**
     * 查询所有班级
     */
    @GetMapping("/list")
    @RequiresPermission({"class:list", "student:list", "student:create", "student:update"})
    public Result findAll() {
        log.info("查询所有班级");
        return Result.success(courseClassService.findAll());
    }

}
