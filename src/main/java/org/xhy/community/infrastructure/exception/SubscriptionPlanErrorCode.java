package org.xhy.community.infrastructure.exception;

public enum SubscriptionPlanErrorCode implements ErrorCode {
    
    SUBSCRIPTION_PLAN_NOT_FOUND(8000, "套餐不存在"),
    SUBSCRIPTION_PLAN_NAME_EXISTS(8001, "套餐名称已存在"),
    SUBSCRIPTION_PLAN_LEVEL_INVALID(8002, "套餐级别必须大于0"),
    SUBSCRIPTION_PLAN_VALIDITY_INVALID(8003, "有效期必须大于0"),
    SUBSCRIPTION_PLAN_PRICE_INVALID(8004, "价格不能为负数");
    
    private final int code;
    private final String message;
    
    SubscriptionPlanErrorCode(int code, String message) {
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