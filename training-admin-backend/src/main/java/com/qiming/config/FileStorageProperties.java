package com.qiming.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 本地文件存储配置(对应 application.yml 中的 file.storage.*)
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    /**
     * 本地存储根目录。相对路径以应用的工作目录为基准。
     */
    private String localDir = "./uploads";

    /**
     * 对外访问前缀。
     * 前端统一使用 /api 前缀, 开发时由 Vite 代理到后端根路径,
     * 所以这里默认带 /api 前缀; 如果浏览器直连后端, 改成 /uploads 即可。
     */
    private String accessPrefix = "/api/uploads";

    /**
     * 单个文件大小上限(字节)
     */
    private long maxSize = 10 * 1024 * 1024L;

}
