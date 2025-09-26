package org.xhy.community.application.user.dto;

import org.xhy.community.domain.user.valueobject.UserRole;
import org.xhy.community.domain.user.valueobject.UserStatus;
import org.xhy.community.application.subscription.dto.UserSubscriptionDTO;

import java.time.LocalDateTime;

public class UserDTO {
    
    private String id;
    private String name;
    private String description;
    private String avatar;
    private String email;
    private UserStatus status;
    private Boolean emailNotificationEnabled;
    private Integer maxConcurrentDevices;
    private UserRole role;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String currentSubscriptionPlanId;
    private String currentSubscriptionPlanName;
    private Integer currentSubscriptionLevel;
    private LocalDateTime currentSubscriptionStartTime;
    private LocalDateTime currentSubscriptionEndTime;
    
    public UserDTO() {
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public Boolean getEmailNotificationEnabled() { return emailNotificationEnabled; }
    public void setEmailNotificationEnabled(Boolean emailNotificationEnabled) { 
        this.emailNotificationEnabled = emailNotificationEnabled; 
    }
    
    public Integer getMaxConcurrentDevices() { return maxConcurrentDevices; }
    public void setMaxConcurrentDevices(Integer maxConcurrentDevices) { 
        this.maxConcurrentDevices = maxConcurrentDevices; 
    }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getCurrentSubscriptionPlanId() {
        return currentSubscriptionPlanId;
    }

    public void setCurrentSubscriptionPlanId(String currentSubscriptionPlanId) {
        this.currentSubscriptionPlanId = currentSubscriptionPlanId;
    }

    public String getCurrentSubscriptionPlanName() {
        return currentSubscriptionPlanName;
    }

    public void setCurrentSubscriptionPlanName(String currentSubscriptionPlanName) {
        this.currentSubscriptionPlanName = currentSubscriptionPlanName;
    }

    public LocalDateTime getCurrentSubscriptionStartTime() {
        return currentSubscriptionStartTime;
    }

    public void setCurrentSubscriptionStartTime(LocalDateTime currentSubscriptionStartTime) {
        this.currentSubscriptionStartTime = currentSubscriptionStartTime;
    }

    public LocalDateTime getCurrentSubscriptionEndTime() {
        return currentSubscriptionEndTime;
    }

    public void setCurrentSubscriptionEndTime(LocalDateTime currentSubscriptionEndTime) {
        this.currentSubscriptionEndTime = currentSubscriptionEndTime;
    }

    public Integer getCurrentSubscriptionLevel() {
        return currentSubscriptionLevel;
    }

    public void setCurrentSubscriptionLevel(Integer currentSubscriptionLevel) {
        this.currentSubscriptionLevel = currentSubscriptionLevel;
    }
}
