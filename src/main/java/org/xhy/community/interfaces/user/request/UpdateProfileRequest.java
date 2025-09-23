package org.xhy.community.interfaces.user.request;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    
    @Size(max = 500, message = "简介长度不能超过500个字符")
    private String description;

    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public UpdateProfileRequest() {
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}