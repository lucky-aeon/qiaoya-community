package org.xhy.community.application.order.dto;

import java.math.BigDecimal;

public class OrderStatisticsDTO {

    private long totalCount;       // 总订单数
    private long purchaseCount;    // 购买订单数
    private long giftCount;        // 赠送订单数
    private BigDecimal totalAmount; // 总金额

    public OrderStatisticsDTO(long totalCount, long purchaseCount, long giftCount, BigDecimal totalAmount) {
        this.totalCount = totalCount;
        this.purchaseCount = purchaseCount;
        this.giftCount = giftCount;
        this.totalAmount = totalAmount;
    }

    // Getters and Setters
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    public long getPurchaseCount() { return purchaseCount; }
    public void setPurchaseCount(long purchaseCount) { this.purchaseCount = purchaseCount; }

    public long getGiftCount() { return giftCount; }
    public void setGiftCount(long giftCount) { this.giftCount = giftCount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}