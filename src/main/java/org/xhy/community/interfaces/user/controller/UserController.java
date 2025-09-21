package org.xhy.community.interfaces.user.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.dto.UserPublicProfileDTO;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.user.request.ChangePasswordRequest;
import org.xhy.community.interfaces.user.request.UpdateProfileRequest;

/**
 * 用户个人信息管理控制器
 * 提供已登录用户的个人信息修改和设置管理功能
 * @module 用户管理
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserAppService userAppService;
    
    public UserController(UserAppService userAppService) {
        this.userAppService = userAppService;
    }
    
    /**
     * 更新用户个人简介
     * 修改当前登录用户的个人简介信息
     * 需要JWT令牌认证
     * 
     * @param request 更新简介请求参数
     *                - description: 个人简介，最大500个字符，可为空
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public ApiResponse<UserDTO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            String userId = UserContext.getCurrentUserId();
            UserDTO user = userAppService.updateProfile(userId, request.getDescription());
            return ApiResponse.success("简介修改成功", user);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    /**
     * 修改用户密码
     * 通过验证原密码来修改为新密码
     * 需要JWT令牌认证
     * 
     * @param request 修改密码请求参数
     *                - oldPassword: 原密码，用于验证用户身份
     *                - newPassword: 新密码，长度6-20位
     * @return 更新后的用户信息
     */
    @PutMapping("/password")
    public ApiResponse<UserDTO> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            String userId = UserContext.getCurrentUserId();
            UserDTO user = userAppService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
            return ApiResponse.success("密码修改成功", user);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    /**
     * 切换邮箱通知设置
     * 切换当前用户的邮箱通知开关状态（开启/关闭）
     * 需要JWT令牌认证
     * 
     * @return 更新后的用户信息，包含新的邮箱通知设置状态
     */
    @PutMapping("/email-notification")
    public ApiResponse<UserDTO> toggleEmailNotification() {
        try {
            String userId = UserContext.getCurrentUserId();
            UserDTO user = userAppService.toggleEmailNotification(userId);
            return ApiResponse.success("邮箱通知设置修改成功", user);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    /**
     * 查看当前用户信息
     * 获取当前登录用户的完整个人信息，包含私密信息（邮箱、通知设置等）
     * 主要用于个人设置页面展示和修改个人信息
     * 需要JWT令牌认证
     * 
     * @return 当前用户的完整信息，包含所有可编辑的字段
     */
    @GetMapping("")
    public ApiResponse<UserDTO> getCurrentUserInfo() {
        try {
            String userId = UserContext.getCurrentUserId();
            UserDTO user = userAppService.getCurrentUserInfo(userId);
            return ApiResponse.success(user);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    /**
     * 查看指定用户信息
     * 获取指定用户的公开个人资料信息，不包含私密信息（邮箱、通知设置等）
     * 主要用于查看其他用户的公开资料
     * 需要JWT令牌认证
     *
     * @param userId 目标用户ID，UUID格式
     * @return 指定用户的公开资料信息
     */
    @GetMapping("/{userId}")
    public ApiResponse<UserPublicProfileDTO> getUserPublicProfile(@PathVariable String userId) {
        try {
            UserPublicProfileDTO user = userAppService.getUserPublicProfile(userId);
            return ApiResponse.success(user);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }

    /**
     * 用户会话心跳检查
     * 用于前端定期检查当前用户会话状态，及时发现被管理员下线的情况
     * 通过拦截器验证会话有效性，如果会话无效将返回401状态码
     * 需要JWT令牌认证
     *
     * @return 会话有效时返回成功状态
     */
    @GetMapping("/heartbeat")
    public ApiResponse<Void> heartbeat() {
        return ApiResponse.success();
    }
}