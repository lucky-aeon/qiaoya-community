package org.xhy.community.interfaces.public_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.infrastructure.config.ApiResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * 公开健康检查接口
 * 路由：/api/public/health
 * 返回标准 ApiResponse，data.status 固定为 "UP"，供反向代理/CI 冒烟使用。
 */
@RestController
@RequestMapping("/api/public")
public class PublicHealthController {

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", System.currentTimeMillis());
        return ApiResponse.success(data);
    }
}

