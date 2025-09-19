package org.xhy.community.application.user.dto;

import org.xhy.community.domain.common.valueobject.ActivityType;
import com.fasterxml.jackson.annotation.JsonFormat;

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
     * 用户昵称
     */
    private String nickname;
    
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 目标类型（如POST、COURSE、USER等）
     */
    private String targetType;
    
    /**
     * 目标对象ID
     */
    private String targetId;
    
    /**
     * 请求路径
     */
    private String requestPath;
    
    /**
     * 扩展上下文数据（JSON格式）
     */
    private String contextData;
    
    /**
     * 目标对象名称（根据targetType和targetId动态获取）
     */
    private String targetName;
    
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
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
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
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public String getTargetType() {
        return targetType;
    }
    
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    
    public String getTargetId() {
        return targetId;
    }
    
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }
    
    public String getRequestPath() {
        return requestPath;
    }
    
    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }
    
    public String getContextData() {
        return contextData;
    }
    
    public void setContextData(String contextData) {
        this.contextData = contextData;
    }
    
    public String getTargetName() {
        return targetName;
    }
    
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
}