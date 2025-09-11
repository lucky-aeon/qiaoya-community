package org.xhy.community.domain.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.entity.BaseEntity;
import org.xhy.community.domain.user.valueobject.UserStatus;

@TableName("users")
public class UserEntity extends BaseEntity {
    
    private String name;
    private String description;
    private String avatar;
    private String email;
    private String password;
    private UserStatus status;
    private Boolean subscribeExternalMessages;
    private Integer maxConcurrentDevices;
    
    public UserEntity() {
    }
    
    public UserEntity(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = UserStatus.ACTIVE;
        this.subscribeExternalMessages = false;
        this.maxConcurrentDevices = 5;
    }
    
    public void updateProfile(String name, String description, String avatar) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        this.description = description;
        this.avatar = avatar;
    }
    
    public void changeEmail(String newEmail) {
        this.email = newEmail;
    }
    
    public void changePassword(String encryptedPassword) {
        this.password = encryptedPassword;
    }
    
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
    
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
    
    public void ban() {
        this.status = UserStatus.BANNED;
    }
    
    public void updateSubscriptionSettings(Boolean subscribeExternalMessages) {
        this.subscribeExternalMessages = subscribeExternalMessages;
    }
    
    public void updateMaxConcurrentDevices(Integer maxDevices) {
        if (maxDevices == null || maxDevices < 1 || maxDevices > 10) {
            throw new IllegalArgumentException("最大并发设备数必须在1-10之间");
        }
        this.maxConcurrentDevices = maxDevices;
    }
    
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }
    
    public Boolean getSubscribeExternalMessages() { return subscribeExternalMessages; }
    public void setSubscribeExternalMessages(Boolean subscribeExternalMessages) { 
        this.subscribeExternalMessages = subscribeExternalMessages; 
    }
    
    public Integer getMaxConcurrentDevices() { return maxConcurrentDevices; }
    public void setMaxConcurrentDevices(Integer maxConcurrentDevices) { 
        this.maxConcurrentDevices = maxConcurrentDevices; 
    }
}