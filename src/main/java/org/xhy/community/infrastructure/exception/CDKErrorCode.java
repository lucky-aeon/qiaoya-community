package org.xhy.community.infrastructure.exception;

public enum CDKErrorCode implements ErrorCode {
    CDK_NOT_FOUND(40400, "CDK不存在"),
    CDK_ALREADY_USED(40001, "CDK已被使用"),
    CDK_NOT_USABLE(40002, "CDK不可用"),
    CDK_CODE_EXISTS(40003, "CDK码已存在"),
    INVALID_CDK_TYPE(40004, "无效的CDK类型"),
    INVALID_TARGET_ID(40005, "无效的目标ID");
    
    private final int code;
    private final String message;
    
    CDKErrorCode(int code, String message) {
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