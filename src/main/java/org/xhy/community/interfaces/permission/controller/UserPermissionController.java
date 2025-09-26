package org.xhy.community.interfaces.permission.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.permission.service.UserPermissionAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

import java.util.List;

/**
 * 用户功能权限码查询
 * 说明：
 * - 用于前端更友好地控制按钮显示/置灰，减少403重试；
 * - 非安全边界，安全由服务端注解 @RequiresPlanPermissions + 拦截器强制校验；
 * - 若采用最小实现，前端可不调用该接口，仅依赖后端403提示。
 */
@RestController
@RequestMapping("/api/user")
public class UserPermissionController {

    private final UserPermissionAppService userPermissionAppService;

    public UserPermissionController(UserPermissionAppService userPermissionAppService) {
        this.userPermissionAppService = userPermissionAppService;
    }

    /** 返回当前用户功能权限码（并集） */
    @GetMapping("/permissions")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "USER_PERMISSIONS_LIST", name = "用户权限码列表")})
    public ApiResponse<List<String>> getUserPermissionCodes() {
        String userId = UserContext.getCurrentUserId();
        return ApiResponse.success(userPermissionAppService.getUserPlanPermissionCodes(userId));
    }
}
