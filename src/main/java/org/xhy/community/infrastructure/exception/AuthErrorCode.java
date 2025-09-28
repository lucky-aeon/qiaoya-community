package org.xhy.community.infrastructure.exception;

public enum AuthErrorCode implements ErrorCode {
    EMAIL_CODE_INVALID(3200, "邮箱验证码无效或已过期"),
    EMAIL_CODE_MISMATCH(3201, "邮箱验证码不正确"),
    EMAIL_SEND_FAILED(3202, "邮件发送失败"),
    IP_RATE_LIMIT_EXCEEDED(3203, "请求频率过高，请稍后再试"),
    IP_BANNED(3204, "当前IP已被封禁"),
    OAUTH_STATE_INVALID(3205, "state 无效或已过期"),
    OAUTH_CODE_EXCHANGE_FAILED(3206, "授权码兑换失败"),
    OAUTH_USERINFO_FAILED(3207, "获取第三方用户信息失败"),
    OAUTH_ALREADY_BOUND(3208, "该第三方账号已绑定其他用户"),
    OAUTH_BIND_CONFLICT(3209, "绑定冲突，请检查账号关系"),
    USER_DISABLED(3210, "用户已被禁用"),
    OAUTH_EMAIL_REQUIRED(3211, "未获取到邮箱，请授权邮箱或稍后重试"),
    PASSWORD_RESET_CODE_INVALID(3212, "密码重置验证码无效或已过期"),
    PASSWORD_RESET_CODE_MISMATCH(3213, "密码重置验证码不正确"),
    PASSWORD_RESET_RATE_LIMIT(3214, "密码重置请求过于频繁，请稍后再试"),
    PASSWORD_RESET_PARAM_INVALID(3215, "密码重置参数无效");

    private final int code;
    private final String message;

    AuthErrorCode(int code, String message) {
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
