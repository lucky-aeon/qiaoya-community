package org.xhy.community.interfaces.subscription.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class SubscriptionPlanQueryRequest extends PageRequest {
    
    private String name;
    
    private Integer level;
    
    public SubscriptionPlanQueryRequest() {
        super();
    }
    
    public SubscriptionPlanQueryRequest(Integer pageNum, Integer pageSize, String name, Integer level) {
        super(pageNum, pageSize);
        this.name = name;
        this.level = level;
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
}