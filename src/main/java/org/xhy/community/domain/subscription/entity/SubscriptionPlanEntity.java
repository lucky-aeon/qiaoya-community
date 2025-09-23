package org.xhy.community.domain.subscription.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.subscription.valueobject.SubscriptionPlanStatus;
import org.xhy.community.infrastructure.converter.SubscriptionPlanStatusConverter;
import org.xhy.community.infrastructure.converter.UniversalListConverter;

import java.math.BigDecimal;
import java.util.List;

@TableName("subscription_plans")
public class SubscriptionPlanEntity extends BaseEntity {
    
    private String name;
    
    private Integer level;
    
    private Integer validityMonths;
    
    private BigDecimal price;

    private BigDecimal originalPrice;

    private Boolean recommended;
    
    @TableField(typeHandler = SubscriptionPlanStatusConverter.class)
    private SubscriptionPlanStatus status;
    
    @TableField(typeHandler = UniversalListConverter.class)
    private List<String> benefits;
    
    public SubscriptionPlanEntity() {
    }
    
    public SubscriptionPlanEntity(String name, Integer level, Integer validityMonths, BigDecimal price, List<String> benefits) {
        this.name = name;
        this.level = level;
        this.validityMonths = validityMonths;
        this.price = price;
        this.benefits = benefits;
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

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public Boolean getRecommended() { return recommended; }
    public void setRecommended(Boolean recommended) { this.recommended = recommended; }
    
    public SubscriptionPlanStatus getStatus() { return status; }
    public void setStatus(SubscriptionPlanStatus status) { this.status = status; }
    
    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }
}
