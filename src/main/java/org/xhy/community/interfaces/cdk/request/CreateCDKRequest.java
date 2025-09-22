package org.xhy.community.interfaces.cdk.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.cdk.valueobject.CDKAcquisitionType;

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

    @DecimalMin(value = "0.00", message = "价格不能为负数")
    private BigDecimal price;

    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
    
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

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}