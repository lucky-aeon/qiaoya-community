package org.xhy.community.interfaces.user.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public class UpdateProfileRequest {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String name;

    @Size(max = 500, message = "简介长度不能超过500个字符")
    private String description;

    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UpdateProfileRequest() {
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}