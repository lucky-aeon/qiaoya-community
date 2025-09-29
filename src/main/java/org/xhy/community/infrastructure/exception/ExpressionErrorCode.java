package org.xhy.community.infrastructure.exception;

public enum ExpressionErrorCode implements ErrorCode {

    EXPRESSION_NOT_FOUND(9000, "表情不存在"),
    EXPRESSION_CODE_EXISTS(9001, "表情代码已存在"),
    EXPRESSION_IN_USE(9002, "该表情正在使用中，无法删除");

    private final int code;
    private final String message;

    ExpressionErrorCode(int code, String message) {
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

