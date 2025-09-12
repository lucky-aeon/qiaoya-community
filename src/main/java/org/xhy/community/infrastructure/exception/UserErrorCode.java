package org.xhy.community.infrastructure.exception;

public enum UserErrorCode implements ErrorCode {
    
    USER_NOT_FOUND(3000, "用户不存在"),
    EMAIL_EXISTS(3001, "该邮箱已被注册"),
    WRONG_PASSWORD(3002, "原密码错误"),
    USER_INACTIVE(3003, "用户未激活"),
    USER_BANNED(3004, "用户已被封禁"),
    EMAIL_NOT_VERIFIED(3005, "邮箱未验证"),
    PROFILE_UPDATE_FAILED(3006, "个人资料更新失败"),
    PASSWORD_CHANGE_FAILED(3007, "密码修改失败"),
    MAX_DEVICES_INVALID(3008, "最大并发设备数必须在1-10之间"),
    USER_REGISTRATION_FAILED(3009, "用户注册失败");
    
    private final int code;
    private final String message;
    
    UserErrorCode(int code, String message) {
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