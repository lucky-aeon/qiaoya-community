package org.xhy.community.interfaces.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.user.dto.AdminUserDTO;
import org.xhy.community.application.user.service.AdminUserAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.user.request.AdminUserQueryRequest;
import org.xhy.community.interfaces.user.request.UpdateUserDevicesRequest;

/**
 * 管理员用户管理控制器
 * 提供管理员对用户的管理功能
 * @module 用户管理
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    
    private final AdminUserAppService adminUserAppService;
    
    public AdminUserController(AdminUserAppService adminUserAppService) {
        this.adminUserAppService = adminUserAppService;
    }
    
    /**
     * 分页查询用户列表
     * 支持邮箱、昵称、状态条件查询
     * 需要管理员权限认证
     * 
     * @param request 查询请求参数
     * @return 用户分页列表
     */
    @GetMapping
    public ApiResponse<IPage<AdminUserDTO>> queryUsers(AdminUserQueryRequest request) {
        IPage<AdminUserDTO> users = adminUserAppService.queryUsers(request);
        return ApiResponse.success(users);
    }
    
    /**
     * 切换用户状态
     * 自动在正常/禁用状态之间切换
     * 需要管理员权限认证
     * 
     * @param userId 用户ID
     * @return 更新后的用户信息
     */
    @PutMapping("/{userId}/toggle-status")
    public ApiResponse<AdminUserDTO> toggleUserStatus(@PathVariable String userId) {
        AdminUserDTO user = adminUserAppService.toggleUserStatus(userId);
        return ApiResponse.success(user);
    }
    
    /**
     * 更新用户设备数量
     * 修改用户最大并发设备数量
     * 需要管理员权限认证
     * 
     * @param userId 用户ID
     * @param request 设备数量更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/{userId}/devices")
    public ApiResponse<AdminUserDTO> updateUserDevices(@PathVariable String userId, @Valid @RequestBody UpdateUserDevicesRequest request) {
        AdminUserDTO user = adminUserAppService.updateUserDevices(userId, request);
        return ApiResponse.success("修改设备数量成功",user);
    }
}