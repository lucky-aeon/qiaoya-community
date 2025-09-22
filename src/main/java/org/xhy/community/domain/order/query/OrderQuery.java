package org.xhy.community.domain.order.query;

import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.order.valueobject.OrderType;
import org.xhy.community.interfaces.common.request.PageRequest;

import java.time.LocalDateTime;

public class OrderQuery extends PageRequest {

    private String userId;
    private OrderType orderType;
    private CDKType productType;
    private String productName;
    private String cdkCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    public CDKType getProductType() { return productType; }
    public void setProductType(CDKType productType) { this.productType = productType; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCdkCode() { return cdkCode; }
    public void setCdkCode(String cdkCode) { this.cdkCode = cdkCode; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}