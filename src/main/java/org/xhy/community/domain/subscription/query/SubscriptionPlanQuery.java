package org.xhy.community.domain.subscription.query;

import org.xhy.community.domain.common.query.BasePageQuery;

public class SubscriptionPlanQuery extends BasePageQuery {
    
    private String name;
    private Integer level;
    private Boolean isActive;
    
    public SubscriptionPlanQuery() {
    }
    
    public SubscriptionPlanQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getLevel() {
        return level;
    }
    
    public void setLevel(Integer level) {
        this.level = level;
    }
    

    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}