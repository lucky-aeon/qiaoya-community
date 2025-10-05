package org.xhy.community.application.user.dto;

import java.time.LocalDateTime;

public class UserPublicProfileDTO {
    
    private String id;
    private String name;
    private String description;
    private String avatar;
    private LocalDateTime createTime;
    private java.util.List<String> tags;
    
    public UserPublicProfileDTO() {
    }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public java.util.List<String> getTags() {
        return tags;
    }

    public void setTags(java.util.List<String> tags) {
        this.tags = tags;
    }
}
