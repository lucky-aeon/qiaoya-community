package org.xhy.community.domain.log.query;

import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.domain.common.valueobject.ActivityCategory;

import java.time.LocalDateTime;

/**
 * 用户活动日志查询对象
 * 用于Domain层的日志查询条件封装
 */
public class UserActivityLogQuery {
    
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
     */
    private ActivityCategory activityCategory;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * IP地址（模糊查询）
     */
    private String ip;
    
    /**
     * 页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
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
    
    public Integer getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}