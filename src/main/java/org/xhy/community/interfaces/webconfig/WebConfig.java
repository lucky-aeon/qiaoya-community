package org.xhy.community.interfaces.webconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserContextInterceptor userContextInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;

    public WebConfig(UserContextInterceptor userContextInterceptor,
                     AdminAuthInterceptor adminAuthInterceptor) {
        this.userContextInterceptor = userContextInterceptor;
        this.adminAuthInterceptor = adminAuthInterceptor;
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
    }
}
