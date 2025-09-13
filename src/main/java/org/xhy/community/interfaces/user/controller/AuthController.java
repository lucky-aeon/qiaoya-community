package org.xhy.community.interfaces.user.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.user.dto.LoginResponseDTO;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.user.request.LoginRequest;
import org.xhy.community.interfaces.user.request.RegisterRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserAppService userAppService;
    
    @Autowired
    public AuthController(UserAppService userAppService) {
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
}