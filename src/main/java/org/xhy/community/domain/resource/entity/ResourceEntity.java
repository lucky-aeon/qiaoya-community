package org.xhy.community.domain.resource.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.resource.valueobject.ResourceType;

@TableName("resources")
public class ResourceEntity extends BaseEntity {
    
    private String fileKey;
    
    private Long size;
    
    private String format;
    
    private String userId;
    
    private ResourceType resourceType;
    
    private String originalName;
    
    public String getFileKey() {
        return fileKey;
    }
    
    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public ResourceType getResourceType() {
        return resourceType;
    }
    
    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
    
    public String getOriginalName() {
        return originalName;
    }
    
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }
    
    public String getFileExtension() {
        if (originalName == null || !originalName.contains(".")) {
            return null;
        }
        return originalName.substring(originalName.lastIndexOf(".") + 1);
    }
    
    public void initializeResourceType() {
        if (this.resourceType == null) {
            this.resourceType = ResourceType.fromFileExtension(getFileExtension());
        }
    }
}