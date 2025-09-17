package org.xhy.community.infrastructure.util.activitylog;

import org.xhy.community.domain.common.valueobject.ActivityType;

/**
 * 活动上下文封装类
 * 包含记录用户活动日志所需的所有上下文信息
 */
public class ActivityContext {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 活动类型
     */
    private ActivityType activityType;
    
    /**
     * 目标类型
     */
    private String targetType;
    
    /**
     * 目标ID
     */
    private String targetId;
    
    /**
     * HTTP请求方法
     */
    private String requestMethod;
    
    /**
     * 请求路径
     */
    private String requestPath;
    
    /**
     * 请求体内容（JSON格式）
     */
    private String requestBody;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * User-Agent字符串
     */
    private String userAgent;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    // 私有构造函数，强制使用Builder模式
    private ActivityContext() {}
    
    // Getters
    public String getUserId() {
        return userId;
    }
    
    public ActivityType getActivityType() {
        return activityType;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public String getRequestMethod() {
        return requestMethod;
    }
    
    public String getRequestPath() {
        return requestPath;
    }
    
    public String getRequestBody() {
        return requestBody;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * 创建Builder实例
     * 
     * @return Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder模式构造器
     */
    public static class Builder {
        private final ActivityContext context;
        
        private Builder() {
            this.context = new ActivityContext();
        }
        
        public Builder userId(String userId) {
            context.userId = userId;
            return this;
        }
        
        public Builder activityType(ActivityType activityType) {
            context.activityType = activityType;
            return this;
        }
        
        public Builder targetType(String targetType) {
            context.targetType = targetType;
            return this;
        }
        
        public Builder targetId(String targetId) {
            context.targetId = targetId;
            return this;
        }
        
        public Builder requestMethod(String requestMethod) {
            context.requestMethod = requestMethod;
            return this;
        }
        
        public Builder requestPath(String requestPath) {
            context.requestPath = requestPath;
            return this;
        }
        
        public Builder requestBody(String requestBody) {
            context.requestBody = requestBody;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            context.ipAddress = ipAddress;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            context.userAgent = userAgent;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            context.sessionId = sessionId;
            return this;
        }
        
        public ActivityContext build() {
            return context;
        }
    }
    
    @Override
    public String toString() {
        return "ActivityContext{" +
                "userId='" + userId + '\'' +
                ", activityType=" + activityType +
                ", targetType='" + targetType + '\'' +
                ", targetId='" + targetId + '\'' +
                ", requestMethod='" + requestMethod + '\'' +
                ", requestPath='" + requestPath + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}