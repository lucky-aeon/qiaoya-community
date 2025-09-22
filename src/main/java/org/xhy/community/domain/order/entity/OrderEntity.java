package org.xhy.community.domain.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.order.valueobject.OrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("orders")
public class OrderEntity extends BaseEntity {

    private String orderNo;              // 订单号
    private String userId;               // 用户ID
    private String cdkCode;              // 关联的CDK码
    private CDKType productType;         // 商品类型
    private String productId;            // 商品ID（targetId）
    private String productName;          // 商品名称
    private OrderType orderType;         // 订单类型
    private BigDecimal amount;           // 金额
    private LocalDateTime activatedTime; // CDK激活时间
    private String remark;               // 备注

    public OrderEntity() {
    }

    public OrderEntity(String orderNo, String userId, String cdkCode, CDKType productType,
                      String productId, String productName, OrderType orderType,
                      BigDecimal amount, LocalDateTime activatedTime) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.cdkCode = cdkCode;
        this.productType = productType;
        this.productId = productId;
        this.productName = productName;
        this.orderType = orderType;
        this.amount = amount;
        this.activatedTime = activatedTime;
    }

    // Getters and Setters
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

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
}