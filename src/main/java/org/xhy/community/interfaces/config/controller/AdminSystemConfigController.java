package org.xhy.community.interfaces.config.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.config.dto.SystemConfigDTO;
import org.xhy.community.application.config.service.AdminSystemConfigAppService;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;
import org.xhy.community.interfaces.config.request.UpdateSystemConfigRequest;

/**
 * 管理员系统配置管理控制器
 * 提供系统配置的查询和更新功能，需要管理员权限
 * @module 系统配置管理
 */
@RestController
@RequestMapping("/api/admin/system-configs")
public class AdminSystemConfigController {

    private final AdminSystemConfigAppService adminSystemConfigAppService;

    public AdminSystemConfigController(AdminSystemConfigAppService adminSystemConfigAppService) {
        this.adminSystemConfigAppService = adminSystemConfigAppService;
    }

    /**
     * 根据类型获取系统配置
     *
     * @param type 配置类型
     * @return 系统配置信息
     */
    @GetMapping("/{type}")
    public ApiResponse<SystemConfigDTO> getConfigByType(@PathVariable String type) {
        SystemConfigType configType = SystemConfigType.valueOf(type.toUpperCase());
        SystemConfigDTO config = adminSystemConfigAppService.getConfigByType(configType);
        return ApiResponse.success(config);
    }

    /**
     * 根据类型更新系统配置
     *
     * @param type 配置类型
     * @param request 更新请求
     * @return 更新后的配置信息
     */
    @PutMapping("/{type}")
    public ApiResponse<SystemConfigDTO> updateConfigByType(
            @PathVariable String type,
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        SystemConfigType configType = SystemConfigType.valueOf(type.toUpperCase());
        SystemConfigDTO config = adminSystemConfigAppService.updateConfigByType(configType, request.getData());
        return ApiResponse.success("保存成功",config);
    }
}