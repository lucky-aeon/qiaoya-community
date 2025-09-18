package org.xhy.community.domain.follow.valueobject;

/**
 * 关注状态枚举
 * 定义关注关系的状态
 */
public enum FollowStatus {
    
    /** 激活状态 - 正在关注 */
    ACTIVE("激活"),
    
    /** 已取消 - 取消关注 */
    CANCELLED("已取消");
    
    private final String description;
    
    FollowStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}