package org.xhy.community.domain.config.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.infrastructure.converter.SystemConfigTypeConverter;

@TableName("system_configs")
public class SystemConfigEntity extends BaseEntity {

    @TableField(typeHandler = SystemConfigTypeConverter.class)
    private SystemConfigType type;

    private String data;

    private String description;

    public SystemConfigEntity() {
    }

    public SystemConfigEntity(SystemConfigType type, String data, String description) {
        this.type = type;
        this.data = data;
        this.description = description;
    }

    public SystemConfigType getType() {
        return type;
    }

    public void setType(SystemConfigType type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}