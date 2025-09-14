package org.xhy.community.infrastructure.exception;

public enum SubscriptionErrorCode implements ErrorCode {
    SUBSCRIPTION_ALREADY_EXISTS(50001, "您已经订阅了该套餐"),
    SUBSCRIPTION_NOT_FOUND(50002, "订阅不存在"),
    SUBSCRIPTION_EXPIRED(50003, "订阅已过期"),
    SUBSCRIPTION_CANCELLED(50004, "订阅已取消"),
    INVALID_SUBSCRIPTION_PERIOD(50005, "无效的订阅周期"),
    COURSE_CDK_ACTIVATION_FAILED(50006, "课程CDK激活失败"),
    PLAN_NOT_FOUND_FOR_CDK(50007, "CDK对应的套餐不存在"),
    COURSE_NOT_FOUND_FOR_CDK(50008, "CDK对应的课程不存在");
    
    private final int code;
    private final String message;
    
    SubscriptionErrorCode(int code, String message) {
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