package org.xhy.community.infrastructure.exception;

/**
 * 标签相关错误码
 */
public enum TagErrorCode implements ErrorCode {

    TAG_NOT_FOUND(7600, "标签不存在"),
    TAG_CODE_EXISTS(7601, "标签code已存在"),
    TAG_DISABLED(7602, "标签已禁用");

    private final int code;
    private final String message;

    TagErrorCode(int code, String message) {
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

