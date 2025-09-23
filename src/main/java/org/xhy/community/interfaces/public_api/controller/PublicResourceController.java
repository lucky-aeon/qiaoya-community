package org.xhy.community.interfaces.public_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.resource.dto.ResourceDTO;
import org.xhy.community.application.resource.service.ResourceAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.resource.request.OssCallbackRequest;

import java.util.Map;

/**
 * 公开资源管理控制器
 * 提供与第三方服务集成的公开接口，无需用户认证
 * @module 公开API
 */
@RestController
@RequestMapping("/api/public")
public class PublicResourceController {
    
    private final ResourceAppService resourceAppService;
    
    public PublicResourceController(ResourceAppService resourceAppService) {
        this.resourceAppService = resourceAppService;
    }
    
    /**
     * 处理阿里云OSS上传回调
     * 接收阿里云OSS服务器的上传成功回调通知，并保存文件元数据
     * 该接口由OSS服务调用，无需用户认证
     * 
     * @param callbackRequest OSS回调请求参数，包含上传文件的元数据信息
     * @param request HTTP请求对象，用于签名验证
     * @return 回调处理结果，返回给OSS服务的响应
     */
    @PostMapping("/oss-callback")
    public ResponseEntity<Map<String, Object>> handleOssCallback(
            @Valid OssCallbackRequest callbackRequest,
            HttpServletRequest request
    ) {
        // OSS回调不需要用户认证，直接处理
        // 简化签名验证 - 可以通过IP白名单或其他方式验证 TODO
        ResourceDTO resource = resourceAppService.handleOssCallback(callbackRequest);
        
        Map<String, Object> response = Map.of("Status", "OK","resource",resource);
        return ResponseEntity.ok(response);
    }
}