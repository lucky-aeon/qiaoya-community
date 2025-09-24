package org.xhy.community.interfaces.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.security.dto.BannedIpDTO;
import org.xhy.community.application.security.service.AdminIpAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

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
     * 解除指定IP的封禁
     */
    @DeleteMapping("/{ip}")
    public ApiResponse<Void> unbanIp(@PathVariable("ip") String ip) {
        adminIpAppService.unbanIp(ip);
        return ApiResponse.success("IP已解除封禁");
    }
}
