package org.xhy.community.interfaces.webconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;
    private final PlanPermissionInterceptor planPermissionInterceptor;

    public WebConfig(UserContextInterceptor userContextInterceptor,
                     AdminAuthInterceptor adminAuthInterceptor,
                     PlanPermissionInterceptor planPermissionInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.planPermissionInterceptor = planPermissionInterceptor;
    }

    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户身份验证拦截器 - 拦截所有API请求
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/public/**", "/api/admin/**");

        // 管理员权限验证拦截器 - 仅拦截管理员接口
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/admin/**");

        // 前台功能权限拦截器 - 仅前台接口，排除 public 与 admin
        registry.addInterceptor(planPermissionInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/public/**", "/api/admin/**");
    }
}
