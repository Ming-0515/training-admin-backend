package com.qiming.controller;

import com.qiming.annotation.RequiresPermission;

import com.qiming.pojo.Result;
import com.qiming.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传
 * 前端头像上传的 action 指向 POST /files/upload
 */
@Slf4j
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * 上传文件, 返回可访问的 URL
     */
    @PostMapping("/upload")
    @RequiresPermission({"employee:create", "employee:update"})
    public Result upload(@RequestParam("file") MultipartFile file) {
        log.info("上传文件: {}", file.getOriginalFilename());
        String url = fileStorageService.upload(file);
        return Result.success(url);
    }

}
