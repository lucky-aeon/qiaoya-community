package org.xhy.community.interfaces.user.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.security.dto.BannedUserDTO;
import org.xhy.community.application.security.service.AdminUserBanAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

import java.util.List;

/**
 * 管理员 用户封禁 管理
 * 路由：/api/admin/user-ban
 */
@RestController
@RequestMapping("/api/admin/user-ban")
public class AdminUserBanController {

    private final AdminUserBanAppService adminUserBanAppService;

    public AdminUserBanController(AdminUserBanAppService adminUserBanAppService) {
        this.adminUserBanAppService = adminUserBanAppService;
    }

    /**
     * 获取当前被封禁的用户列表
     */
    @GetMapping
    public ApiResponse<List<BannedUserDTO>> listBannedUsers() {
        List<BannedUserDTO> list = adminUserBanAppService.listBannedUsers();
        return ApiResponse.success(list);
    }

    /**
     * 解除指定用户的封禁
     */
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> unbanUser(@PathVariable("userId") String userId) {
        adminUserBanAppService.unbanUser(userId);
        return ApiResponse.success("用户已解除封禁");
    }
}

