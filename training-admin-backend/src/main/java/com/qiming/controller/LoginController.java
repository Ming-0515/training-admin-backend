package com.qiming.controller;

import com.qiming.pojo.Employee;
import com.qiming.pojo.LoginInfo;
import com.qiming.pojo.Result;
import com.qiming.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录
     */
    @PostMapping
    public Result login(@RequestBody Employee employee) {
        log.info("登录请求参数: {}", employee);
        LoginInfo loginInfo = employeeService.login(employee);
        return Result.success(loginInfo);
    }

}
