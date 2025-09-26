package org.xhy.community.interfaces.subscription.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.subscription.service.UserMenuAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

import java.util.List;

/**
 * 用户菜单码查询
 * 前端必须依赖该接口返回的 menuCodes 过滤菜单可见性（不同套餐看到不同菜单）。
 */
@RestController
@RequestMapping("/api/user")
public class UserMenuController {

    private final UserMenuAppService userMenuAppService;

    public UserMenuController(UserMenuAppService userMenuAppService) {
        this.userMenuAppService = userMenuAppService;
    }

    /** 返回当前用户可见菜单码（并集） */
    @GetMapping("/menu-codes")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "USER_MENU_CODES", name = "用户菜单码列表")})
    public ApiResponse<List<String>> getUserMenuCodes() {
        String userId = UserContext.getCurrentUserId();
        return ApiResponse.success(userMenuAppService.getUserMenuCodes(userId));
    }
}
