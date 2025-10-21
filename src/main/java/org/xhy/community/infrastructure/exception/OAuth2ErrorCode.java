package org.xhy.community.infrastructure.exception;

/**
 * OAuth2 授权服务器异常错误码
 * 错误码范围：3300-3399
 */
public enum OAuth2ErrorCode implements ErrorCode {

    // 客户端相关错误 (3300-3309)
    CLIENT_NOT_FOUND(3300, "OAuth2客户端不存在"),
    CLIENT_ID_ALREADY_EXISTS(3301, "客户端ID已存在"),
    CLIENT_SUSPENDED(3302, "客户端已被暂停"),
    CLIENT_REVOKED(3303, "客户端已被撤销"),
    INVALID_CLIENT_CREDENTIALS(3304, "客户端认证失败"),

    // 授权请求相关错误 (3310-3319)
    INVALID_REDIRECT_URI(3310, "无效的重定向URI"),
    INVALID_GRANT_TYPE(3311, "不支持的授权类型"),
    INVALID_SCOPE(3312, "无效的Scope"),
    AUTHORIZATION_DENIED(3313, "用户拒绝授权"),
    UNAUTHORIZED(3314, "用户未登录或认证失败"),

    // 授权码相关错误 (3320-3329)
    INVALID_AUTHORIZATION_CODE(3320, "无效的授权码"),
    EXPIRED_AUTHORIZATION_CODE(3321, "授权码已过期"),

    // Token相关错误 (3330-3339)
    INVALID_ACCESS_TOKEN(3330, "无效的Access Token"),
    EXPIRED_ACCESS_TOKEN(3331, "Access Token已过期"),
    INVALID_REFRESH_TOKEN(3332, "无效的Refresh Token"),
    EXPIRED_REFRESH_TOKEN(3333, "Refresh Token已过期");

    private final int code;
    private final String message;

    OAuth2ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
