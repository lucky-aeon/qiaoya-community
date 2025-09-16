package org.xhy.community.domain.subscription.query;

import org.xhy.community.domain.common.query.BasePageQuery;

public class SubscriptionQuery extends BasePageQuery {
    
    private String userId;
    private String planId;
    private Boolean isActive;
    
    public SubscriptionQuery() {
    }
    
    public SubscriptionQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getPlanId() {
        return planId;
    }
    
    public void setPlanId(String planId) {
        this.planId = planId;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}