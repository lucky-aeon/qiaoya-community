package org.xhy.community.interfaces.user.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.security.dto.BannedIpDTO;
import org.xhy.community.application.security.service.AdminIpAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.interfaces.user.request.BanIpRequest;

import java.util.List;

/**
 * 管理员 IP 封禁管理
 * 路由：/api/admin/ip-ban
 */
@RestController
@RequestMapping("/api/admin/ip-ban")
public class AdminIpController {

    private final AdminIpAppService adminIpAppService;

    public AdminIpController(AdminIpAppService adminIpAppService) {
        this.adminIpAppService = adminIpAppService;
    }

    /**
     * 获取当前被封禁的IP列表
     */
    @GetMapping
    public ApiResponse<List<BannedIpDTO>> listBannedIps() {
        List<BannedIpDTO> list = adminIpAppService.listBannedIps();
        return ApiResponse.success(list);
    }

    /**
     * 管理员封禁指定IP
     */
    @PostMapping
    @ActivityLog(ActivityType.ADMIN_IP_BAN)
    public ApiResponse<Void> banIp(@Valid @RequestBody BanIpRequest request) {
        adminIpAppService.banIp(request);
        return ApiResponse.success("IP已封禁");
    }

    /**
     * 解除指定IP的封禁
     */
    @DeleteMapping("/{ip:.+}")
    @ActivityLog(ActivityType.ADMIN_IP_UNBAN)
    public ApiResponse<Void> unbanIp(@PathVariable("ip") String ip) {
        adminIpAppService.unbanIp(ip);
        return ApiResponse.success("IP已解除封禁");
    }
}
