package org.xhy.community.domain.tag.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.tag.valueobject.TagTargetType;
import org.xhy.community.infrastructure.converter.TagTargetTypeConverter;

@TableName("tag_scopes")
public class TagScopeEntity extends BaseEntity {
    private String tagId;
    @TableField(typeHandler = TagTargetTypeConverter.class)
    private TagTargetType targetType;
    private String targetId;

    public String getTagId() { return tagId; }
    public void setTagId(String tagId) { this.tagId = tagId; }
    public TagTargetType getTargetType() { return targetType; }
    public void setTargetType(TagTargetType targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
}
