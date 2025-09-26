package org.xhy.community.interfaces.subscription.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.subscription.service.AdminSubscriptionPlanPermissionAppService;
import org.xhy.community.application.subscription.dto.PermissionOptionDTO;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.subscription.request.UpdateSubscriptionPlanPermissionsRequest;

import java.util.List;

/**
 * 管理员套餐-权限绑定管理
 * 路由：/api/admin/subscription-plan-permissions
 */
@RestController
@RequestMapping("/api/admin/subscription-plan-permissions")
public class AdminSubscriptionPlanPermissionController {

    private final AdminSubscriptionPlanPermissionAppService adminSubscriptionPlanPermissionAppService;

    public AdminSubscriptionPlanPermissionController(AdminSubscriptionPlanPermissionAppService adminSubscriptionPlanPermissionAppService) {
        this.adminSubscriptionPlanPermissionAppService = adminSubscriptionPlanPermissionAppService;
    }

    /** 获取权限选项清单（用于绑定界面） */
    @GetMapping("/options")
    public ApiResponse<List<PermissionOptionDTO>> getPermissionOptions() {
        return ApiResponse.success(adminSubscriptionPlanPermissionAppService.getPermissionOptions());
    }

    /** 获取项目中通过注解声明的权限码（动态扫描） */
    @GetMapping("/discovered")
    public ApiResponse<List<String>> getDiscoveredPermissionCodes() {
        return ApiResponse.success(adminSubscriptionPlanPermissionAppService.getDiscoveredPermissionCodes());
    }

    /** 获取套餐已绑定的权限码 */
    @GetMapping("/{planId}")
    public ApiResponse<List<String>> getPlanPermissionCodes(@PathVariable String planId) {
        return ApiResponse.success(adminSubscriptionPlanPermissionAppService.getSubscriptionPlanPermissionCodes(planId));
    }

    /** 全量更新套餐的权限绑定 */
    @PutMapping("/{planId}")
    public ApiResponse<Void> updatePlanPermissions(@PathVariable String planId,
                                                   @Valid @RequestBody UpdateSubscriptionPlanPermissionsRequest request) {
        adminSubscriptionPlanPermissionAppService.updateSubscriptionPlanPermissions(planId, request);
        return ApiResponse.success("绑定成功");
    }
}
