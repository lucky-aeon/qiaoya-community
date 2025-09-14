package org.xhy.community.application.post.dto;

import org.xhy.community.domain.post.valueobject.CategoryType;

import java.time.LocalDateTime;
import java.util.List;

public class CategoryTreeDTO {
    
    private String id;
    private String name;
    private String parentId;
    private CategoryType type;
    private Integer level;
    private Integer sortOrder;
    private String description;
    private String icon;
    private Boolean isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<CategoryTreeDTO> children;
    
    public CategoryTreeDTO() {
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    
    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    public List<CategoryTreeDTO> getChildren() { return children; }
    public void setChildren(List<CategoryTreeDTO> children) { this.children = children; }
}