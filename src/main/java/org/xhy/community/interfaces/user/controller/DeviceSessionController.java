package org.xhy.community.interfaces.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.session.dto.ActiveSessionDTO;
import org.xhy.community.application.session.service.DeviceSessionAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.infrastructure.util.ClientIpUtil;

import java.util.List;

/**
 * 用户设备会话控制器
 * 提供用户查看和管理自己设备会话的功能
 */
@RestController
@RequestMapping("/api/user/sessions")
public class DeviceSessionController {

    private final DeviceSessionAppService deviceSessionAppService;

    public DeviceSessionController(DeviceSessionAppService deviceSessionAppService) {
        this.deviceSessionAppService = deviceSessionAppService;
    }

    /**
     * 查看用户活跃设备列表
     * 显示当前用户所有活跃IP及最后活跃时间，并标记当前请求的IP
     */
    @GetMapping("/active")
    public ApiResponse<List<ActiveSessionDTO>> getActiveSessions(HttpServletRequest request) {
        try {
            String userId = UserContext.getCurrentUserId();
            String currentIp = ClientIpUtil.getClientIp(request);

            List<ActiveSessionDTO> activeSessions = deviceSessionAppService.getUserActiveSessions(userId, currentIp);
            return ApiResponse.success("获取活跃设备列表成功", activeSessions);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 用户主动下线指定IP的设备
     * 用户可以主动断开某个IP的登录会话
     */
    @DeleteMapping("/active/{ip}")
    public ApiResponse<Void> removeActiveSession(@PathVariable String ip) {
        try {
            String userId = UserContext.getCurrentUserId();
            deviceSessionAppService.removeUserActiveSession(userId, ip);
            return ApiResponse.success("设备下线成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}