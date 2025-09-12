package org.xhy.community.interfaces.user.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.user.dto.LoginResponseDTO;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.user.request.ChangePasswordRequest;
import org.xhy.community.interfaces.user.request.LoginRequest;
import org.xhy.community.interfaces.user.request.RegisterRequest;
import org.xhy.community.interfaces.user.request.UpdateProfileRequest;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserAppService userAppService;
    
    @Autowired
    public UserController(UserAppService userAppService) {
        this.userAppService = userAppService;
    }
    
    @PostMapping("/login")
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponseDTO loginResponse = userAppService.login(request.getEmail(), request.getPassword());
            return ApiResponse.success("登录成功", loginResponse);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ApiResponse<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        try {
            UserDTO user = userAppService.register(
                request.getEmail(), 
                request.getEmailVerificationCode(), 
                request.getPassword()
            );
            return ApiResponse.success("注册成功", user);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, e.getMessage());
        }
    }
    
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
}