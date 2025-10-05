package org.xhy.community.domain.tag.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

@TableName("tag_definitions")
public class TagDefinitionEntity extends BaseEntity {
    private String code;
    private String name;
    private String category;
    private String iconUrl;
    private String description;
    private Boolean publicVisible;
    private Boolean uniquePerUser;
    private Boolean enabled;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
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

