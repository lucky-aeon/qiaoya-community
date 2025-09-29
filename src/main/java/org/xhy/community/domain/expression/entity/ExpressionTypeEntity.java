package org.xhy.community.domain.expression.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

@TableName("expression_types")
public class ExpressionTypeEntity extends BaseEntity {

    /** 表情代码（Markdown 使用，不含冒号） */
    private String code;

    /** 表情名称（展示名） */
    private String name;

    /** 图片URL（或相对路径） */
    private String imageUrl;

    /** 排序（越小越靠前） */
    private Integer sortOrder;

    /** 是否启用 */
    private Boolean isActive;

    public ExpressionTypeEntity() {}

    public ExpressionTypeEntity(String code, String name, String imageUrl) {
        this.code = code;
        this.name = name;
        this.imageUrl = imageUrl;
        this.sortOrder = 0;
        this.isActive = true;
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}

