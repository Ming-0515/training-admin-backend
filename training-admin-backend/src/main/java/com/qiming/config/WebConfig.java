package com.qiming.config;

import com.qiming.interceptor.OperationLogInterceptor;
import com.qiming.interceptor.PermissionInterceptor;
import com.qiming.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;
    @Autowired
    private OperationLogInterceptor operationLogInterceptor;
    @Autowired
    private PermissionInterceptor permissionInterceptor;
    @Autowired
    private FileStorageProperties fileStorageProperties;

    /**
     * 注册拦截器, 顺序很关键:
     * <ol>
     *     <li>TokenInterceptor —— 判断有没有登录, 并把当前登录人写入 UserContext</li>
     *     <li>OperationLogInterceptor —— 记录操作日志; 放在权限校验之前,
     *         这样"越权被拒"这件事本身也会留下日志</li>
     *     <li>PermissionInterceptor —— RBAC 权限校验, 不通过直接返回 403</li>
     * </ol>
     * 若把权限校验排到日志拦截器之前, 越权请求会在权限校验处中断,
     * 日志拦截器的 afterCompletion 不会执行, 越权行为就没有记录了。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                // /uploads/** 是上传文件的访问地址, 由 <img> 标签直接请求, 无法携带 token, 必须放行
                .excludePathPatterns("/login", "/uploads/**");

        registry.addInterceptor(operationLogInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**");
    }

    /**
     * 把本地上传目录映射为静态资源, 使上传的图片可以直接通过 /uploads/** 访问。
     * 注意: 这里的访问路径不带 /api 前缀, 前缀由 nginx 的 /api/ 代理规则补充。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(fileStorageProperties.getLocalDir())
                .toAbsolutePath().normalize().toString().replace('\\', '/');
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location + "/");
    }

}
