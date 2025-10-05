package org.xhy.community.domain.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.user.valueobject.UserStatus;
import org.xhy.community.domain.user.valueobject.UserRole;
import org.xhy.community.infrastructure.converter.UserStatusConverter;
import org.xhy.community.infrastructure.converter.UserRoleConverter;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.UserErrorCode;

@TableName("users")
public class UserEntity extends BaseEntity {
    
    /** 用户姓名 */
    private String name;
    
    /** 用户个人描述/简介 */
    private String description;
    
    /** 用户头像地址 */
    private String avatar;
    
    /** 用户邮箱，用于登录和通知 */
    private String email;
    
    /** 用户密码，加密存储 */
    private String password;
    
    /** 用户状态：ACTIVE-活跃，INACTIVE-未激活，BANNED-已封禁 */
    @TableField(typeHandler = UserStatusConverter.class)
    private UserStatus status;

    /** 用户角色：USER-普通用户，ADMIN-管理员 */
    @TableField(typeHandler = UserRoleConverter.class)
    private UserRole role;
    
    /** 是否启用邮件通知 */
    private Boolean emailNotificationEnabled;
    
    /** 最大并发设备数，范围：1-10 */
    private Integer maxConcurrentDevices;
    
    public UserEntity() {
    }
    
    public UserEntity(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = UserStatus.ACTIVE;
        this.role = UserRole.USER;
        this.emailNotificationEnabled = false;
        this.maxConcurrentDevices = 1;
    }

    public UserEntity(String name, String email, String password, String avatar) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.status = UserStatus.ACTIVE;
        this.role = UserRole.USER;
        this.emailNotificationEnabled = false;
        this.maxConcurrentDevices = 1;
    }
    
    public void updateProfile(String name, String description, String avatar) {
        if (StringUtils.hasText(name)) {
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
    
    public void toggleStatus() {
        this.status = (this.status == UserStatus.ACTIVE) ? UserStatus.INACTIVE : UserStatus.ACTIVE;
    }
    
    public void updateEmailNotificationSettings(Boolean emailNotificationEnabled) {
        this.emailNotificationEnabled = emailNotificationEnabled;
    }
    
    public void updateMaxConcurrentDevices(Integer maxDevices) {
        if (maxDevices == null || maxDevices < 1 || maxDevices > 10) {
            throw new BusinessException(
                UserErrorCode.MAX_DEVICES_INVALID
            );
        }
        this.maxConcurrentDevices = maxDevices;
    }
    
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(this.status);
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
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

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public Boolean getEmailNotificationEnabled() { return emailNotificationEnabled; }
    public void setEmailNotificationEnabled(Boolean emailNotificationEnabled) { 
        this.emailNotificationEnabled = emailNotificationEnabled; 
    }
    
    public Integer getMaxConcurrentDevices() { return maxConcurrentDevices; }
    public void setMaxConcurrentDevices(Integer maxConcurrentDevices) { 
        this.maxConcurrentDevices = maxConcurrentDevices; 
    }
}
