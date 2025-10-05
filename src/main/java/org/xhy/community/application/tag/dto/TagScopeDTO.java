package org.xhy.community.application.tag.dto;

import org.xhy.community.domain.tag.entity.TagScopeEntity;
import org.xhy.community.domain.tag.valueobject.TagTargetType;

import java.time.LocalDateTime;

public class TagScopeDTO {
    private String id;
    private TagTargetType targetType;
    private String targetId;
    private LocalDateTime createTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public TagTargetType getTargetType() { return targetType; }
    public void setTargetType(TagTargetType targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public static TagScopeDTO fromEntity(TagScopeEntity e) {
        if (e == null) return null;
        TagScopeDTO dto = new TagScopeDTO();
        dto.setId(e.getId());
        dto.setTargetType(e.getTargetType());
        dto.setTargetId(e.getTargetId());
        dto.setCreateTime(e.getCreateTime());
        return dto;
    }
}

