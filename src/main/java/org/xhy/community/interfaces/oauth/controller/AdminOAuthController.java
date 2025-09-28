package org.xhy.community.interfaces.oauth.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.auth.dto.AdminSocialAccountDTO;
import org.xhy.community.application.auth.service.AdminAuthAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.oauth.request.AdminSocialAccountQueryRequest;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;

@RestController
@RequestMapping("/api/admin/auth/social-accounts")
public class AdminOAuthController {

    private final AdminAuthAppService adminAuthAppService;

    public AdminOAuthController(AdminAuthAppService adminAuthAppService) {
        this.adminAuthAppService = adminAuthAppService;
    }

    @GetMapping
    public ApiResponse<com.baomidou.mybatisplus.core.metadata.IPage<AdminSocialAccountDTO>> page(AdminSocialAccountQueryRequest request) {
        return ApiResponse.success(adminAuthAppService.pageSocialAccounts(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<AdminSocialAccountDTO> get(@PathVariable String id) {
        return ApiResponse.success(adminAuthAppService.getById(id));
    }

    @PostMapping("/{id}/unbind")
    @ActivityLog(ActivityType.ADMIN_OAUTH_UNBIND)
    public ApiResponse<Void> unbind(@PathVariable String id) {
        adminAuthAppService.adminUnbindById(id);
        return ApiResponse.success("解绑成功");
    }
}
