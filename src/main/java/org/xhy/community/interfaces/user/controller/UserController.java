package org.xhy.community.interfaces.user.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.user.request.ChangePasswordRequest;
import org.xhy.community.interfaces.user.request.UpdateProfileRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserAppService userAppService;
    
    public UserController(UserAppService userAppService) {
        this.userAppService = userAppService;
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