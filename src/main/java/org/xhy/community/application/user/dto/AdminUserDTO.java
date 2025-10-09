package org.xhy.community.application.user.dto;

import org.xhy.community.domain.user.valueobject.UserStatus;

import java.time.LocalDateTime;

/**
 * 管理员用户DTO
 * 管理员视图的用户信息，包含完整的用户数据
 */
public class AdminUserDTO {
    
    private String id;
    private String name;
    private String description;
    private String avatar;
    private String email;
    private UserStatus status;
    private Boolean emailNotificationEnabled;
    private Integer maxConcurrentDevices;
    private String currentPlanName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    public AdminUserDTO() {
    }
    
    // Getters and Setters
    public String getId() { 
        return id; 
    }
    
    public void setId(String id) { 
        this.id = id; 
    }
    
    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public void setDescription(String description) { 
        this.description = description; 
    }
    
    public String getAvatar() { 
        return avatar; 
    }
    
    public void setAvatar(String avatar) { 
        this.avatar = avatar; 
    }
    
    public String getEmail() { 
        return email; 
    }
    
    public void setEmail(String email) { 
        this.email = email; 
    }
    
    public UserStatus getStatus() { 
        return status; 
    }
    
    public void setStatus(UserStatus status) { 
        this.status = status; 
    }
    
    public Boolean getEmailNotificationEnabled() { 
        return emailNotificationEnabled; 
    }
    
    public void setEmailNotificationEnabled(Boolean emailNotificationEnabled) { 
        this.emailNotificationEnabled = emailNotificationEnabled; 
    }
    
    public Integer getMaxConcurrentDevices() {
        return maxConcurrentDevices;
    }

    public void setMaxConcurrentDevices(Integer maxConcurrentDevices) {
        this.maxConcurrentDevices = maxConcurrentDevices;
    }

    public String getCurrentPlanName() {
        return currentPlanName;
    }

    public void setCurrentPlanName(String currentPlanName) {
        this.currentPlanName = currentPlanName;
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
}