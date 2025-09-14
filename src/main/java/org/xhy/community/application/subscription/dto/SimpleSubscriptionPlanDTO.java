package org.xhy.community.application.subscription.dto;

public class SimpleSubscriptionPlanDTO {
    
    private String id;
    private String name;
    private Integer level;
    
    public SimpleSubscriptionPlanDTO() {
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
}