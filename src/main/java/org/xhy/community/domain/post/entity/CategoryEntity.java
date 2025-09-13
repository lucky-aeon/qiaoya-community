package org.xhy.community.domain.post.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.entity.BaseEntity;
import org.xhy.community.domain.post.valueobject.CategoryType;

@TableName("categories")
public class CategoryEntity extends BaseEntity {
    
    /** 分类名称 */
    private String name;
    
    /** 父分类ID，为空表示顶级分类 */
    private String parentId;
    
    /** 分类类型：ARTICLE-文章，QA-问答 */
    private CategoryType type;
    
    /** 分类层级，从1开始 */
    private Integer level;
    
    /** 排序序号，用于控制显示顺序 */
    private Integer sortOrder;
    
    /** 分类描述 */
    private String description;
    
    /** 分类图标 */
    private String icon;
    
    /** 是否激活，false表示禁用 */
    private Boolean isActive;
    
    public CategoryEntity() {
    }
    
    public CategoryEntity(String name, CategoryType type) {
        this.name = name;
        this.type = type;
        this.level = 1;
        this.sortOrder = 0;
        this.isActive = true;
    }
    
    public CategoryEntity(String name, String parentId, CategoryType type, Integer level) {
        this.name = name;
        this.parentId = parentId;
        this.type = type;
        this.level = level;
        this.sortOrder = 0;
        this.isActive = true;
    }
    
    // Getters and Setters
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
}