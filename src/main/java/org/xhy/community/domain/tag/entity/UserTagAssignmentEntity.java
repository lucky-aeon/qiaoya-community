package org.xhy.community.domain.tag.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.tag.valueobject.TagAssignmentStatus;
import org.xhy.community.domain.tag.valueobject.TagSourceType;
import org.xhy.community.infrastructure.converter.TagAssignmentStatusConverter;
import org.xhy.community.infrastructure.converter.TagSourceTypeConverter;
import org.xhy.community.infrastructure.converter.MapJsonTypeHandler;

import java.time.LocalDateTime;
import java.util.Map;

@TableName("user_tag_assignments")
public class UserTagAssignmentEntity extends BaseEntity {
    private String userId;
    private String tagId;

    @TableField(typeHandler = TagAssignmentStatusConverter.class)
    private TagAssignmentStatus status;

    private LocalDateTime issuedAt;
    private LocalDateTime revokedAt;
    private LocalDateTime expireAt;

    @TableField(typeHandler = TagSourceTypeConverter.class)
    private TagSourceType sourceType;
    private String sourceId;

    @TableField(typeHandler = MapJsonTypeHandler.class, value = "meta")
    private Map<String, Object> meta;

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
    public TagAssignmentStatus getStatus() { return status; }
    public void setStatus(TagAssignmentStatus status) { this.status = status; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
    public TagSourceType getSourceType() { return sourceType; }
    public void setSourceType(TagSourceType sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }
}
