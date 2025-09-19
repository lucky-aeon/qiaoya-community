package org.xhy.community.interfaces.log.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.xhy.community.interfaces.common.request.PageRequest;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.domain.common.valueobject.ActivityCategory;

import java.time.LocalDateTime;

/**
 * 查询用户活动日志请求对象
 * 继承PageRequest以支持分页查询
 */
public class QueryUserActivityLogRequest extends PageRequest {
    
    /**
     * 用户ID（精确查询）
     */
    private String userId;
    
    
    /**
     * 活动类型（精确查询单个类型）
     */
    private ActivityType activityType;
    
    /**
     * 活动分类（分类查询，查询该分类下所有类型）
     * 与activityType互斥，不能同时指定
     */
    private ActivityCategory activityCategory;
    
    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /**
     * IP地址（模糊查询）
     */
    private String ip;
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public ActivityType getActivityType() {
        return activityType;
    }
    
    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }
    
    public ActivityCategory getActivityCategory() {
        return activityCategory;
    }
    
    public void setActivityCategory(ActivityCategory activityCategory) {
        this.activityCategory = activityCategory;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
}