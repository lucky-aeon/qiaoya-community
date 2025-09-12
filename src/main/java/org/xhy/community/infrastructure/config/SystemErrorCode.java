package org.xhy.community.infrastructure.config;

import org.xhy.community.infrastructure.exception.ErrorCode;

public enum SystemErrorCode implements ErrorCode {
    
    INTERNAL_ERROR(1000, "系统内部错误"),
    SERVICE_UNAVAILABLE(1001, "服务不可用"),
    DATABASE_ERROR(1002, "数据库操作失败"),
    NETWORK_ERROR(1003, "网络连接异常"),
    TIMEOUT_ERROR(1004, "操作超时");
    
    private final int code;
    private final String message;
    
    SystemErrorCode(int code, String message) {
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