package org.xhy.community.application.order.dto;

import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.order.valueobject.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDTO {

    private String id;
    private String orderNo;
    private String userId;
    private String userName;
    private String cdkCode;
    private CDKType productType;
    private String productId;
    private String productName;
    private OrderType orderType;
    private BigDecimal amount;
    private LocalDateTime activatedTime;
    private String remark;
    private LocalDateTime createTime;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getCdkCode() { return cdkCode; }
    public void setCdkCode(String cdkCode) { this.cdkCode = cdkCode; }

    public CDKType getProductType() { return productType; }
    public void setProductType(CDKType productType) { this.productType = productType; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public OrderType getOrderType() { return orderType; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public LocalDateTime getActivatedTime() { return activatedTime; }
    public void setActivatedTime(LocalDateTime activatedTime) { this.activatedTime = activatedTime; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}