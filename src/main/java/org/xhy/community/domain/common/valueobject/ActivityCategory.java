package org.xhy.community.domain.common.valueobject;

/**
 * 活动分类枚举
 * 用于对用户活动类型进行分类，便于统计分析和权限控制
 */
public enum ActivityCategory {
    
    /**
     * 认证相关
     */
    AUTHENTICATION("认证相关"),
    
    /**
     * 内容浏览
     */
    BROWSING("内容浏览"),
    
    /**
     * 内容创作
     */
    CONTENT_CREATION("内容创作"),
    
    /**
     * 社交互动
     */
    SOCIAL_INTERACTION("社交互动"),
    
    /**
     * 学习行为
     */
    LEARNING("学习行为"),
    
    /**
     * 管理操作
     */
    ADMINISTRATION("管理操作"),
    
    /**
     * 其他
     */
    OTHER("其他");
    
    private final String description;
    
    ActivityCategory(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}