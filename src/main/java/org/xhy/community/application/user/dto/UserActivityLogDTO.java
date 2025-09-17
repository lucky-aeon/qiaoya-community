package org.xhy.community.application.user.dto;

import org.xhy.community.domain.common.valueobject.ActivityType;

import java.time.LocalDateTime;

/**
 * 用户活动日志数据传输对象
 */
public class UserActivityLogDTO {
    
    /**
     * 主键ID
     */
    private String id;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户邮箱
     */
    private String email;
    
    /**
     * 活动类型
     */
    private ActivityType activityType;
    
    /**
     * 活动类型描述
     */
    private String activityTypeDesc;
    
    /**
     * 浏览器信息
     */
    private String browser;
    
    /**
     * 设备信息
     */
    private String equipment;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * User-Agent信息
     */
    private String userAgent;
    
    /**
     * 失败原因
     */
    private String failureReason;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public ActivityType getActivityType() {
        return activityType;
    }
    
    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }
    
    public String getActivityTypeDesc() {
        return activityTypeDesc;
    }
    
    public void setActivityTypeDesc(String activityTypeDesc) {
        this.activityTypeDesc = activityTypeDesc;
    }
    
    public String getBrowser() {
        return browser;
    }
    
    public void setBrowser(String browser) {
        this.browser = browser;
    }
    
    public String getEquipment() {
        return equipment;
    }
    
    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}