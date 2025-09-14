package org.xhy.community.interfaces.post.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.xhy.community.domain.post.valueobject.CategoryType;

public class CreateCategoryRequest {
    
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 50, message = "分类名称长度必须在2-50字符之间")
    private String name;
    
    private String parentId;
    
    @NotNull(message = "分类类型不能为空")
    private CategoryType type;
    
    private Integer sortOrder;
    
    @Size(max = 200, message = "分类描述长度不能超过200字符")
    private String description;
    
    @Size(max = 100, message = "分类图标长度不能超过100字符")
    private String icon;
    
    public CreateCategoryRequest() {
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getParentId() { return parentId; }
    public void setParentId(String parentId) { this.parentId = parentId; }
    
    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}