package org.xhy.community.infrastructure.exception;

public enum SystemConfigErrorCode implements ErrorCode {
    CONFIG_NOT_FOUND(7001, "系统配置不存在"),
    CONFIG_PARSE_ERROR(7002, "系统配置解析失败"),
    CONFIG_SERIALIZE_ERROR(7003, "系统配置序列化失败"),
    INVALID_CONFIG_DATA(7004, "无效的配置数据");

    private final int code;
    private final String message;

    SystemConfigErrorCode(int code, String message) {
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