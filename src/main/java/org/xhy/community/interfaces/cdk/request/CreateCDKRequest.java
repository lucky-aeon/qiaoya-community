package org.xhy.community.interfaces.cdk.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.xhy.community.domain.cdk.valueobject.CDKType;

/**
 * 创建CDK请求
 */
public class CreateCDKRequest {
    
    @NotNull(message = "CDK类型不能为空")
    private CDKType cdkType;
    
    @NotBlank(message = "目标ID不能为空")
    private String targetId;
    
    @Min(value = 1, message = "生成数量至少为1")
    private int quantity = 1;
    
    public CreateCDKRequest() {
    }
    
    public CDKType getCdkType() { return cdkType; }
    public void setCdkType(CDKType cdkType) { this.cdkType = cdkType; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}