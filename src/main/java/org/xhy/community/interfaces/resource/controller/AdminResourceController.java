package org.xhy.community.interfaces.resource.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.resource.dto.PagedResourceDTO;
import org.xhy.community.application.resource.service.AdminResourceAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.resource.request.ResourceQueryRequest;

/**
 * 管理员资源控制器
 * 提供管理员查看全站资源的分页列表
 * @module 资源管理（管理员）
 */
@RestController
@RequestMapping("/api/admin/resources")
public class AdminResourceController {

    private final AdminResourceAppService adminResourceAppService;

    public AdminResourceController(AdminResourceAppService adminResourceAppService) {
        this.adminResourceAppService = adminResourceAppService;
    }

    /**
     * 分页获取资源列表（管理员）
     * 支持按资源类型筛选
     */
    @GetMapping
    public ApiResponse<PagedResourceDTO> getResources(@Valid ResourceQueryRequest request) {
        PagedResourceDTO resources = adminResourceAppService.getResources(request);
        return ApiResponse.success(resources);
    }
}

