package org.xhy.community.application.subscription.dto;

import org.xhy.community.domain.subscription.valueobject.SubscriptionPlanStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SubscriptionPlanDTO {
    
    private String id;
    
    private String name;
    
    private Integer level;
    
    private Integer validityMonths;
    
    private BigDecimal price;
    
    private SubscriptionPlanStatus status;
    
    private List<String> benefits;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    public SubscriptionPlanDTO() {
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    public Integer getValidityMonths() { return validityMonths; }
    public void setValidityMonths(Integer validityMonths) { this.validityMonths = validityMonths; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public SubscriptionPlanStatus getStatus() { return status; }
    public void setStatus(SubscriptionPlanStatus status) { this.status = status; }
    
    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}