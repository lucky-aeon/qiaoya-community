package org.xhy.community.interfaces.public_api.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.resource.dto.ResourceDTO;
import org.xhy.community.application.resource.service.ResourceAppService;
import org.xhy.community.interfaces.resource.request.OssCallbackRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicResourceController {
    
    private final ResourceAppService resourceAppService;
    
    public PublicResourceController(ResourceAppService resourceAppService) {
        this.resourceAppService = resourceAppService;
    }
    
    @PostMapping("/oss-callback")
    public ResponseEntity<Map<String, String>> handleOssCallback(
            @Valid OssCallbackRequest callbackRequest,
            HttpServletRequest request
    ) {
        // OSS回调不需要用户认证，直接处理
        // 简化签名验证 - 可以通过IP白名单或其他方式验证
        ResourceDTO resource = resourceAppService.handleOssCallback(callbackRequest);
        
        Map<String, String> response = Map.of("Status", "OK");
        return ResponseEntity.ok(response);
    }
}