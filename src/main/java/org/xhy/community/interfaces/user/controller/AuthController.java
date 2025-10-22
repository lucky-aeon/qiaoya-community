package org.xhy.community.interfaces.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.user.dto.LoginResponseDTO;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.EnvironmentConfig;
import org.xhy.community.infrastructure.config.JwtUtil;
import org.xhy.community.infrastructure.util.ClientIpUtil;
import org.xhy.community.interfaces.user.request.LoginRequest;
import org.xhy.community.interfaces.user.request.RegisterRequest;
import org.xhy.community.interfaces.user.request.SendEmailCodeRequest;
import org.xhy.community.interfaces.user.request.SendPasswordResetCodeRequest;
import org.xhy.community.interfaces.user.request.ResetPasswordRequest;
import org.xhy.community.infrastructure.annotation.LogUserActivity;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.springframework.util.StringUtils;

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

    private final EnvironmentConfig environmentConfig;

    public AuthController(UserAppService userAppService, JwtUtil jwtUtil, EnvironmentConfig environmentConfig) {
        this.userAppService = userAppService;
        this.jwtUtil = jwtUtil;
        this.environmentConfig = environmentConfig;
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
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        String ip = ClientIpUtil.getClientIp(httpRequest);
        String deviceId = extractDeviceId(httpRequest);
        LoginResponseDTO loginResponse = userAppService.login(request.getEmail(), request.getPassword(), ip, deviceId);

        // 设置 RAUTH Cookie 用于 CDN 资源访问鉴权
        setRauthCookie(httpRequest, httpResponse, loginResponse.getToken());

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
    public ApiResponse<HashMap<String, Object>> register(@Valid @RequestBody RegisterRequest request,
                                                         HttpServletRequest httpRequest,
                                                         HttpServletResponse httpResponse) {
        UserDTO user = userAppService.register(
                request.getEmail(),
                request.getEmailVerificationCode(),
                request.getPassword()
        );
        HashMap<String, Object> res = new HashMap<>();
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        res.put("user",user);
        res.put("token",token);

        // 设置 RAUTH Cookie 用于 CDN 资源访问鉴权
        setRauthCookie(httpRequest, httpResponse, token);

        return ApiResponse.success("注册成功", res);
    }

    /**
     * 发送密码重置验证码
     */
    @PostMapping("/password/reset-code")
    public ApiResponse<Void> sendPasswordResetCode(@Valid @RequestBody SendPasswordResetCodeRequest request,
                                                   HttpServletRequest httpRequest) {
        String ip = ClientIpUtil.getClientIp(httpRequest);
        userAppService.sendPasswordResetCode(request.getEmail(), ip);
        return ApiResponse.success("密码重置验证码已发送");
    }

    /**
     * 重置密码
     */
    @PostMapping("/password/reset")
    @LogUserActivity(
        successType = ActivityType.RESET_PASSWORD,
        failureType = ActivityType.RESET_PASSWORD
    )
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userAppService.resetPassword(request.getEmail(), request.getVerificationCode(), request.getNewPassword());
        return ApiResponse.success("密码重置成功");
    }

    /**
     * 退出登录（当前设备）
     * - 将当前token加入黑名单（剩余TTL）
     * - 移除token与IP映射
     * - 从活跃设备集合移除当前IP
     * - 幂等：无论token是否有效，都返回成功
     */
    @PostMapping("/logout")
    @ActivityLog(ActivityType.LOGOUT)
    public ApiResponse<HashMap<String, Object>> logout(HttpServletRequest httpRequest,
                                                       HttpServletResponse httpResponse) {
        String authorization = httpRequest.getHeader("Authorization");
        String ip = ClientIpUtil.getClientIp(httpRequest);
        String deviceId = extractDeviceId(httpRequest);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            if (jwtUtil.validateToken(token)) {
                String userId = jwtUtil.getUserIdFromToken(token);
                userAppService.logout(userId, token, ip, deviceId);
            }
        }

        // 清除 RAUTH Cookie
        clearRauthCookie(httpRequest, httpResponse);

        HashMap<String, Object> data = new HashMap<>();
        data.put("message", "退出成功");
        return ApiResponse.success("退出成功", data);
    }

    private String extractDeviceId(HttpServletRequest request) {
        // 优先请求头
        String deviceId = request.getHeader("X-Device-ID");
        if (StringUtils.hasText(deviceId)) {
            return deviceId;
        }
        // 其次 Cookie（名称 DID）
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if ("DID".equals(c.getName()) && StringUtils.hasText(c.getValue())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 设置 RAUTH Cookie 用于 CDN 资源访问鉴权
     * Cookie 配置：
     * - Domain: LOCAL环境不设置domain，其他环境设置.xhyovo.cn (支持所有子域名，包括 oss.xhyovo.cn)
     * - Path: / (全局有效)
     * - MaxAge: 30天 (与 JWT 过期时间一致)
     * - HttpOnly: true (防止 XSS 攻击)
     * - Secure: 根据请求协议动态判断
     * - SameSite: Lax (防止 CSRF 攻击)
     */
    private void setRauthCookie(HttpServletRequest request, HttpServletResponse response, String token) {
        boolean isSecure = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("RAUTH", token)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(2592000); // 30天（秒）

        // 根据环境决定是否设置 domain
        String domain = environmentConfig.getCookieDomain();
        if (domain != null) {
            cookieBuilder.domain(domain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
    }

    /**
     * 清除 RAUTH Cookie
     */
    private void clearRauthCookie(HttpServletRequest request, HttpServletResponse response) {
        boolean isSecure = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("RAUTH", "")
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0); // 立即过期

        // 根据环境决定是否设置 domain（必须与设置时保持一致）
        String domain = environmentConfig.getCookieDomain();
        if (domain != null) {
            cookieBuilder.domain(domain);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
    }
}
