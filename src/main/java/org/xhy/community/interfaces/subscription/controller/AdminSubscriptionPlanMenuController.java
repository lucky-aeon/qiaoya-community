package org.xhy.community.interfaces.subscription.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.subscription.dto.MenuOptionDTO;
import org.xhy.community.application.subscription.service.AdminSubscriptionPlanMenuAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.subscription.request.UpdateSubscriptionPlanMenusRequest;

import java.util.List;

/**
 * 管理员套餐-菜单绑定管理
 * 路由：/api/admin/subscription-plan-menus
 */
@RestController
@RequestMapping("/api/admin/subscription-plan-menus")
public class AdminSubscriptionPlanMenuController {

    private final AdminSubscriptionPlanMenuAppService adminSubscriptionPlanMenuAppService;

    public AdminSubscriptionPlanMenuController(AdminSubscriptionPlanMenuAppService adminSubscriptionPlanMenuAppService) {
        this.adminSubscriptionPlanMenuAppService = adminSubscriptionPlanMenuAppService;
    }

    /** 获取菜单选项清单（用于绑定界面） */
    @GetMapping("/options")
    public ApiResponse<List<MenuOptionDTO>> getMenuOptions() {
        return ApiResponse.success(adminSubscriptionPlanMenuAppService.getMenuOptions());
    }

    /** 获取套餐已绑定的菜单码 */
    @GetMapping("/{planId}")
    public ApiResponse<List<String>> getPlanMenuCodes(@PathVariable String planId) {
        return ApiResponse.success(adminSubscriptionPlanMenuAppService.getSubscriptionPlanMenuCodes(planId));
    }

    /** 全量更新套餐的菜单绑定 */
    @PutMapping("/{planId}")
    public ApiResponse<Void> updatePlanMenus(@PathVariable String planId,
                                             @Valid @RequestBody UpdateSubscriptionPlanMenusRequest request) {
        adminSubscriptionPlanMenuAppService.updateSubscriptionPlanMenus(planId, request);
        return ApiResponse.success("绑定成功");
    }
}

