package com.qiming.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务
 */
public interface FileStorageService {

    /**
     * 保存上传的文件, 返回可访问的 URL
     */
    String upload(MultipartFile file);

}
