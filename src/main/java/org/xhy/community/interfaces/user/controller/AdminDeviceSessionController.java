package org.xhy.community.interfaces.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.session.dto.DeviceSessionQuery;
import org.xhy.community.application.session.dto.UserSessionSummaryDTO;
import org.xhy.community.application.session.service.AdminDeviceSessionAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.user.request.AdminDeviceSessionQueryRequest;

/**
 * 管理员设备会话管理控制器
 * 提供管理员查看和管理用户设备会话的功能
 * @module 设备会话管理
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminDeviceSessionController {

    private final AdminDeviceSessionAppService adminDeviceSessionAppService;

    public AdminDeviceSessionController(AdminDeviceSessionAppService adminDeviceSessionAppService) {
        this.adminDeviceSessionAppService = adminDeviceSessionAppService;
    }

    /**
     * 分页查询用户设备会话信息
     * 管理员可以查看所有用户的活跃设备列表，支持按用户ID、用户名、IP过滤
     *
     * @param request 查询请求参数
     *                - pageNum: 页码，默认1
     *                - pageSize: 每页数量，默认20
     *                - userId: 可选，筛选特定用户
     *                - username: 可选，按用户名搜索
     *                - ip: 可选，按IP搜索
     * @return 用户设备会话分页列表
     */
    @GetMapping("/sessions")
    public ApiResponse<IPage<UserSessionSummaryDTO>> queryUserSessions(AdminDeviceSessionQueryRequest request) {
        try {
            DeviceSessionQuery query = new DeviceSessionQuery(request.getPageNum(), request.getPageSize());
            query.setUserId(request.getUserId());
            query.setUsername(request.getUsername());
            query.setIp(request.getIp());

            IPage<UserSessionSummaryDTO> result = adminDeviceSessionAppService.queryUserSessions(query);
            return ApiResponse.success("查询用户设备会话成功", result);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 强制下线指定用户的指定IP设备
     * 管理员可以强制断开用户某个IP的登录会话
     *
     * @param userId 用户ID
     * @param ip 要下线的IP地址
     * @return 操作结果
     */
    @DeleteMapping("/{userId}/sessions/ip/{ip}")
    public ApiResponse<Void> forceRemoveUserSession(
            @PathVariable String userId,
            @PathVariable String ip) {
        try {
            adminDeviceSessionAppService.forceRemoveUserSession(userId, ip);
            return ApiResponse.success("强制下线设备成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 强制下线指定用户的所有设备
     * 管理员可以强制断开用户所有设备的登录会话
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}/sessions/all")
    public ApiResponse<Void> forceRemoveAllUserSessions(@PathVariable String userId) {
        try {
            adminDeviceSessionAppService.forceRemoveAllUserSessions(userId);
            return ApiResponse.success("强制下线用户所有设备成功", null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
}