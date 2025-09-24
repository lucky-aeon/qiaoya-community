package org.xhy.community.infrastructure.exception;

public enum AuthErrorCode implements ErrorCode {
    EMAIL_CODE_INVALID(3200, "邮箱验证码无效或已过期"),
    EMAIL_CODE_MISMATCH(3201, "邮箱验证码不正确"),
    EMAIL_SEND_FAILED(3202, "邮件发送失败"),
    IP_RATE_LIMIT_EXCEEDED(3203, "请求频率过高，请稍后再试"),
    IP_BANNED(3204, "当前IP已被封禁");

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

