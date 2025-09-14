package org.xhy.community.domain.subscription.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.entity.BaseEntity;
import org.xhy.community.domain.subscription.valueobject.SubscriptionPlanStatus;
import org.xhy.community.infrastructure.converter.SubscriptionPlanStatusConverter;

import java.math.BigDecimal;

@TableName("subscription_plans")
public class SubscriptionPlanEntity extends BaseEntity {
    
    private String name;
    
    private Integer level;
    
    private Integer validityMonths;
    
    private BigDecimal price;
    
    @TableField(typeHandler = SubscriptionPlanStatusConverter.class)
    private SubscriptionPlanStatus status;
    
    private String description;
    
    public SubscriptionPlanEntity() {
    }
    
    public SubscriptionPlanEntity(String name, Integer level, Integer validityMonths, BigDecimal price, String description) {
        this.name = name;
        this.level = level;
        this.validityMonths = validityMonths;
        this.price = price;
        this.description = description;
        this.status = SubscriptionPlanStatus.ACTIVE;
    }
    
    public void activate() {
        this.status = SubscriptionPlanStatus.ACTIVE;
    }
    
    public void deactivate() {
        this.status = SubscriptionPlanStatus.INACTIVE;
    }
    
    public boolean isActive() {
        return SubscriptionPlanStatus.ACTIVE.equals(this.status);
    }
    
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
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}