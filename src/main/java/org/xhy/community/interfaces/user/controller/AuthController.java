package org.xhy.community.interfaces.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.user.dto.LoginResponseDTO;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.JwtUtil;
import org.xhy.community.infrastructure.util.ClientIpUtil;
import org.xhy.community.interfaces.user.request.LoginRequest;
import org.xhy.community.interfaces.user.request.RegisterRequest;
import org.xhy.community.interfaces.user.request.SendEmailCodeRequest;
import org.xhy.community.infrastructure.annotation.LogUserActivity;
import org.xhy.community.domain.common.valueobject.ActivityType;

import java.util.HashMap;

/**
 * 用户认证控制器
 * 提供用户登录、注册等认证相关功能
 * @module 用户认证
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserAppService userAppService;

    private final JwtUtil jwtUtil;

    public AuthController(UserAppService userAppService, JwtUtil jwtUtil) {
        this.userAppService = userAppService;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * 用户登录
     * 通过邮箱和密码进行用户登录认证，成功后返回JWT令牌
     * 
     * @param request 登录请求参数
     *                - email: 用户邮箱地址，必须为有效的邮箱格式
     *                - password: 用户密码，不能为空
     * @return 登录响应数据，包含JWT令牌和用户基本信息
     */
    @PostMapping("/login")
    @LogUserActivity(
        successType = ActivityType.LOGIN_SUCCESS,
        failureType = ActivityType.LOGIN_FAILED
    )
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = ClientIpUtil.getClientIp(httpRequest);
        LoginResponseDTO loginResponse = userAppService.login(request.getEmail(), request.getPassword(), ip);
        return ApiResponse.success("登录成功", loginResponse);
    }

    /**
     * 发送注册邮箱邀请码
     * 进行IP频控：同一IP每天最多3次；超过后封禁7天
     */
    @PostMapping("/register/email-code")
    public ApiResponse<Void> sendRegisterEmailCode(@Valid @RequestBody SendEmailCodeRequest request, HttpServletRequest httpRequest) {
        String ip = ClientIpUtil.getClientIp(httpRequest);
        userAppService.sendRegisterEmailCode(request.getEmail(), ip);
        return ApiResponse.success("邮箱邀请码已发送");
    }
    
    /**
     * 用户注册
     * 通过邮箱验证码完成用户注册，注册成功后返回用户信息
     * 注册前需要先通过其他接口获取邮箱验证码
     * 
     * @param request 注册请求参数
     *                - email: 用户邮箱地址，必须为有效的邮箱格式，系统中不能已存在
     *                - emailVerificationCode: 邮箱验证码，6位数字，通过邮件获取
     *                - password: 用户密码，长度6-20位
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    @LogUserActivity(
        successType = ActivityType.REGISTER_SUCCESS,
        failureType = ActivityType.REGISTER_FAILED
    )
    public ApiResponse<HashMap<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = userAppService.register(
                request.getEmail(),
                request.getEmailVerificationCode(),
                request.getPassword()
        );
        HashMap<String, Object> res = new HashMap<>();
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        res.put("user",user);
        res.put("token",token);
        return ApiResponse.success("注册成功", res);
    }
}
