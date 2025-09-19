package org.xhy.community.domain.notification.valueobject;

/**
 * 通知类型枚举
 */
public enum NotificationType {
    
    NEW_FOLLOWER("新关注者"),
    FOLLOWED_USER_POST("关注用户发布新内容"),
    CDK_ACTIVATED("CDK激活成功"),
    SUBSCRIPTION_EXPIRED("订阅即将过期"),
    POST_COMMENT("文章被评论"),
    COURSE_COMMENT("课程被评论");
    
    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCode() {
        return this.name();
    }
}