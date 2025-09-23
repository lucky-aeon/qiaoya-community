package org.xhy.community.interfaces.subscription.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

public class CreateSubscriptionPlanRequest {
    
    @NotBlank(message = "套餐名称不能为空")
    @Size(min = 2, max = 100, message = "套餐名称长度必须在2-100字符之间")
    private String name;
    
    @NotNull(message = "套餐级别不能为空")
    @Min(value = 1, message = "套餐级别必须大于0")
    private Integer level;
    
    @NotNull(message = "有效期不能为空")
    @Min(value = 1, message = "有效期必须大于0个月")
    private Integer validityMonths;
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.00", message = "价格不能为负数")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "原价不能为负数")
    private BigDecimal originalPrice;

    private Boolean recommended;
    
    @Valid
    @Size(max = 20, message = "权益列表最多包含20项")
    private List<@Size(min = 1, max = 200, message = "权益描述长度必须在1-200字符之间") String> benefits;
    
    public CreateSubscriptionPlanRequest() {
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    public Integer getValidityMonths() { return validityMonths; }
    public void setValidityMonths(Integer validityMonths) { this.validityMonths = validityMonths; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public Boolean getRecommended() { return recommended; }
    public void setRecommended(Boolean recommended) { this.recommended = recommended; }
    
    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }
}
