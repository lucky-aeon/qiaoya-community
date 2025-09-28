package org.xhy.community.interfaces.cdk.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.cdk.valueobject.CDKAcquisitionType;
import org.xhy.community.domain.cdk.valueobject.CDKSubscriptionStrategy;

import java.math.BigDecimal;


/**
 * 创建CDK请求
 */
public class CreateCDKRequest {
    
    @NotNull(message = "CDK类型不能为空")
    private CDKType cdkType;

    @NotBlank(message = "目标ID不能为空")
    private String targetId;

    @Min(value = 1, message = "生成数量至少为1")
    @Max(value = 1000, message = "单次创建数量不能超过1000")
    private int quantity = 1;

    @NotNull(message = "获得方式不能为空")
    private CDKAcquisitionType acquisitionType;

    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;

    // 新增字段
    private BigDecimal price; // 可选：覆盖价格/补差价
    private CDKSubscriptionStrategy subscriptionStrategy; // 可选：套餐策略（仅套餐型CDK有效，默认PURCHASE）
    
    public CreateCDKRequest() {
    }
    
    public CDKType getCdkType() { return cdkType; }
    public void setCdkType(CDKType cdkType) { this.cdkType = cdkType; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public CDKAcquisitionType getAcquisitionType() { return acquisitionType; }
    public void setAcquisitionType(CDKAcquisitionType acquisitionType) { this.acquisitionType = acquisitionType; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public CDKSubscriptionStrategy getSubscriptionStrategy() { return subscriptionStrategy; }
    public void setSubscriptionStrategy(CDKSubscriptionStrategy subscriptionStrategy) { this.subscriptionStrategy = subscriptionStrategy; }
}
