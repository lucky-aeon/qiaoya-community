package org.xhy.community.infrastructure.exception;

public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(50400, "订单不存在"),
    ORDER_ALREADY_EXISTS(50001, "订单已存在"),
    INVALID_ORDER_DATA(50002, "订单数据无效"),
    ORDER_CREATION_FAILED(50003, "订单创建失败");

    private final int code;
    private final String message;

    OrderErrorCode(int code, String message) {
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