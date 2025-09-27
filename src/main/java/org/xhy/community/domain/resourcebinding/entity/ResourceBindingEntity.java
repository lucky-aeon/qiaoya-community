package org.xhy.community.domain.resourcebinding.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.resourcebinding.valueobject.ResourceTargetType;
import org.xhy.community.infrastructure.converter.ResourceTargetTypeConverter;

@TableName("resource_bindings")
public class ResourceBindingEntity extends BaseEntity {

    private String resourceId;

    @TableField(typeHandler = ResourceTargetTypeConverter.class)
    private ResourceTargetType targetType;

    private String targetId;

    public ResourceBindingEntity() {}

    public ResourceBindingEntity(String resourceId, ResourceTargetType targetType, String targetId) {
        this.resourceId = resourceId;
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public ResourceTargetType getTargetType() { return targetType; }
    public void setTargetType(ResourceTargetType targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
}

