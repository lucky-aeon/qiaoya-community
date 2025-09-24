package org.xhy.community.interfaces.public_api.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.user.dto.UserStatsDTO;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

/**
 * 公开统计信息控制器
 * 提供公开访问的统计信息接口，无需用户认证
 * 路由前缀：/api/public/stats
 */
@RestController
@RequestMapping("/api/public/stats")
public class PublicStatsController {

    private final UserAppService userAppService;

    public PublicStatsController(UserAppService userAppService) {
        this.userAppService = userAppService;
    }

    /**
     * 获取社区用户统计信息
     *
     * @return 用户统计信息，包含总注册人数
     */
    @GetMapping("/users")
    public ApiResponse<UserStatsDTO> getUserStats() {
        UserStatsDTO stats = userAppService.getUserStats();
        return ApiResponse.success(stats);
    }
}