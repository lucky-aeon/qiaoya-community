package org.xhy.community.interfaces.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.dto.UserPublicProfileDTO;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.user.request.ChangePasswordRequest;
import org.xhy.community.interfaces.user.request.UpdateProfileRequest;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

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
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "USER_PROFILE_UPDATE", name = "修改个人资料")})
    public ApiResponse<UserDTO> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String userId = UserContext.getCurrentUserId();
        UserDTO user = userAppService.updateProfile(userId, request);
        return ApiResponse.success("个人资料修改成功", user);
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
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "USER_PASSWORD_CHANGE", name = "修改密码")})
    public ApiResponse<UserDTO> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        String userId = UserContext.getCurrentUserId();
        UserDTO user = userAppService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success("密码修改成功", user);
    }
    
    /**
     * 切换邮箱通知设置
     * 切换当前用户的邮箱通知开关状态（开启/关闭）
     * 需要JWT令牌认证
     * 
     * @return 更新后的用户信息，包含新的邮箱通知设置状态
     */
    @PutMapping("/email-notification")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "USER_EMAIL_NOTIFICATION_TOGGLE", name = "切换邮箱通知")})
    public ApiResponse<UserDTO> toggleEmailNotification() {
        String userId = UserContext.getCurrentUserId();
        UserDTO user = userAppService.toggleEmailNotification(userId);
        return ApiResponse.success("邮箱通知设置修改成功", user);
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
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "USER_INFO_CURRENT", name = "当前用户信息")})
    public ApiResponse<UserDTO> getCurrentUserInfo() {
        String userId = UserContext.getCurrentUserId();
        UserDTO user = userAppService.getCurrentUserInfo(userId);
        return ApiResponse.success(user);
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
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "USER_PUBLIC_PROFILE_VIEW", name = "查看用户公开资料")})
    public ApiResponse<UserPublicProfileDTO> getUserPublicProfile(@PathVariable String userId) {
        UserPublicProfileDTO user = userAppService.getUserPublicProfile(userId);
        return ApiResponse.success(user);
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
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "USER_HEARTBEAT", name = "会话心跳")})
    public ApiResponse<Void> heartbeat(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 续签资源访问会话 Cookie（RAUTH），确保 <img>/<a> 访问不中断
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            boolean isSecure = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
            ResponseCookie cookie = ResponseCookie.from("RAUTH", token)
                    .httpOnly(true)
                    .secure(isSecure)
                    .sameSite("Lax")
                    .path("/api/public/resource")
                    .maxAge(900) // 15分钟滑动续签
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return ApiResponse.success();
    }
}
