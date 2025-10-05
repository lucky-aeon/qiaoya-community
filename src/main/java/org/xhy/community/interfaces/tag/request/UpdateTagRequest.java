package org.xhy.community.interfaces.tag.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateTagRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String category;
    private String iconUrl;
    private String description;
    private Boolean publicVisible;
    private Boolean uniquePerUser;
    private Boolean enabled;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getPublicVisible() { return publicVisible; }
    public void setPublicVisible(Boolean publicVisible) { this.publicVisible = publicVisible; }
    public Boolean getUniquePerUser() { return uniquePerUser; }
    public void setUniquePerUser(Boolean uniquePerUser) { this.uniquePerUser = uniquePerUser; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}

