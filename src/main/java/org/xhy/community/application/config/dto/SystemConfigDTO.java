package org.xhy.community.application.config.dto;

import org.xhy.community.domain.config.valueobject.SystemConfigType;

import java.time.LocalDateTime;

public class SystemConfigDTO {

    private String id;
    private SystemConfigType type;
    private Object data;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public SystemConfigDTO() {
    }

    public SystemConfigDTO(String id, SystemConfigType type, Object data, String description,
                          LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.type = type;
        this.data = data;
        this.description = description;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SystemConfigType getType() {
        return type;
    }

    public void setType(SystemConfigType type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}