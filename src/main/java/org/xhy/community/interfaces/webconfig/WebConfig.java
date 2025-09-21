package org.xhy.community.interfaces.webconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    
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
