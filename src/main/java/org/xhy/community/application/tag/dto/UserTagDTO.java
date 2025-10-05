package org.xhy.community.application.tag.dto;

import java.time.LocalDateTime;
import java.util.Map;
import org.xhy.community.domain.tag.valueobject.TagSourceType;

public class UserTagDTO {
    private String tagId;
    private String code;
    private String name;
    private String category;
    private String iconUrl;
    private String description;
    private LocalDateTime issuedAt;
    private TagSourceType sourceType;
    private String sourceId;
    private Map<String, Object> meta;

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
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
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public TagSourceType getSourceType() { return sourceType; }
    public void setSourceType(TagSourceType sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }
}
