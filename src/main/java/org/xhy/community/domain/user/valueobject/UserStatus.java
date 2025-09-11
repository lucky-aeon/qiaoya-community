package org.xhy.community.domain.user.valueobject;

public enum UserStatus {
    ACTIVE("正常"),
    INACTIVE("禁用"),
    BANNED("封禁");
    
    private final String description;
    
    UserStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}