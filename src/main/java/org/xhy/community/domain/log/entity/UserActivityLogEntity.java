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
    private LocalDateTime createTime;
    
    /**
     * 更新时间（重写BaseEntity中的字段以支持自定义字段名）
     */
    @TableField("updated_at")
    private LocalDateTime updateTime;
    
    // ==================== 业务扩展字段（新增） ====================
    
    /**
     * 目标类型（如POST、COURSE、USER等）
     */
    @TableField("target_type")
    private String targetType;
    
    /**
     * 目标对象ID
     */
    @TableField("target_id")
    private String targetId;
    
    /**
     * HTTP请求方法
     */
    @TableField("request_method")
    private String requestMethod;
    
    /**
     * 请求路径
     */
    @TableField("request_path")
    private String requestPath;
    
    /**
     * 执行时间（毫秒）
     */
    @TableField("execution_time_ms")
    private Integer executionTimeMs;
    
    /**
     * 会话ID
     */
    @TableField("session_id")
    private String sessionId;
    
    /**
     * 扩展上下文数据（JSON格式）
     */
    @TableField("context_data")
    private String contextData;
    
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
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    // ==================== 业务扩展字段的Getter/Setter（新增） ====================
    
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
    
    public String getRequestMethod() {
        return requestMethod;
    }
    
    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
    
    public String getRequestPath() {
        return requestPath;
    }
    
    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }
    
    public Integer getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    public void setExecutionTimeMs(Integer executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public String getContextData() {
        return contextData;
    }
    
    public void setContextData(String contextData) {
        this.contextData = contextData;
    }
}