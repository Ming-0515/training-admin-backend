package com.qiming.service.impl;

import com.qiming.config.FileStorageProperties;
import com.qiming.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 本地磁盘文件存储实现。
 * <p>
 * 取代原先的阿里云 OSS 方案: 不再依赖外部云服务与访问凭证,
 * 上传的文件按 yyyy/MM 分目录存到本地, 通过静态资源映射对外提供访问。
 */
@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    /** 允许上传的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp");

    private static final DateTimeFormatter DIR_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    @Autowired
    private FileStorageProperties properties;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空!");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("只支持上传 jpg/jpeg/png/gif/bmp/webp 格式的图片!");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("只支持上传图片文件!");
        }

        if (file.getSize() > properties.getMaxSize()) {
            throw new IllegalArgumentException("图片大小不能超过 " + (properties.getMaxSize() / 1024 / 1024) + "MB!");
        }

        // 按 年/月 分目录, 文件名用 UUID 避免重名与路径穿越
        String dir = LocalDate.now().format(DIR_FORMATTER);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;

        try {
            Path baseDir = Paths.get(properties.getLocalDir()).toAbsolutePath().normalize();
            Path targetDir = baseDir.resolve(dir).normalize();
            // 双保险: 归一化后必须仍在根目录之内
            if (!targetDir.startsWith(baseDir)) {
                throw new IllegalArgumentException("非法的上传路径!");
            }
            Files.createDirectories(targetDir);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, targetDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("保存上传文件失败", e);
            throw new IllegalStateException("文件保存失败, 请稍后重试!");
        }

        String url = properties.getAccessPrefix() + "/" + dir + "/" + fileName;
        log.info("文件已保存到本地: {} -> {}", file.getOriginalFilename(), url);
        return url;
    }

    /**
     * 取扩展名(小写, 不含点)。没有扩展名时返回空串, 由调用方判为非法。
     */
    private String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

}
