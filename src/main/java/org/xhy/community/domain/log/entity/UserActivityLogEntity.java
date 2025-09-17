package org.xhy.community.domain.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.valueobject.ActivityType;

import java.time.LocalDateTime;

/**
 * 用户活动日志实体
 * 记录用户的登录、注册等活动信息
 */
@TableName("user_activity_logs")
public class UserActivityLogEntity  {


    /** 主键ID，使用UUID自动生成 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID，登录失败时可能为null
     */
    @TableField("user_id")
    private String userId;
    
    /**
     * 用户邮箱
     */
    @TableField("email")
    private String email;
    
    /**
     * 活动类型
     */
    @TableField("activity_type")
    private ActivityType activityType;
    
    /**
     * 浏览器信息
     */
    @TableField("browser")
    private String browser;
    
    /**
     * 设备信息
     */
    @TableField("equipment")
    private String equipment;
    
    /**
     * IP地址
     */
    @TableField("ip")
    private String ip;
    
    /**
     * 完整的User-Agent信息
     */
    @TableField("user_agent")
    private String userAgent;
    
    /**
     * 失败原因，成功时为null
     */
    @TableField("failure_reason")
    private String failureReason;
    
    /**
     * 创建时间（重写BaseEntity中的字段以支持自定义字段名）
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间（重写BaseEntity中的字段以支持自定义字段名）
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    // Getters and Setters
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}