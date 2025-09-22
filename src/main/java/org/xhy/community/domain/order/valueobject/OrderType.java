package org.xhy.community.domain.order.valueobject;

/**
 * 订单类型枚举
 */
public enum OrderType {
    PURCHASE("购买订单"),
    GIFT("赠送订单");

    private final String description;

    OrderType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}