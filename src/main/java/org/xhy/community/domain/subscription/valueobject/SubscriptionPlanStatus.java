package org.xhy.community.domain.subscription.valueobject;

public enum SubscriptionPlanStatus {
    
    ACTIVE("激活"),
    INACTIVE("停用");
    
    private final String description;
    
    SubscriptionPlanStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}