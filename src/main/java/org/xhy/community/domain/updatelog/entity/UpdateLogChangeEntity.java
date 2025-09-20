package org.xhy.community.domain.updatelog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.updatelog.valueobject.ChangeType;

@TableName("update_log_changes")
public class UpdateLogChangeEntity extends BaseEntity {

    private String updateLogId;
    private ChangeType type;
    private String title;
    private String description;
    private String category;
    private Integer sortOrder;

    public UpdateLogChangeEntity() {}

    public UpdateLogChangeEntity(String updateLogId, ChangeType type, String title, String description, String category, Integer sortOrder) {
        this.updateLogId = updateLogId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.category = category;
        this.sortOrder = sortOrder;
    }

    public String getUpdateLogId() {
        return updateLogId;
    }

    public void setUpdateLogId(String updateLogId) {
        this.updateLogId = updateLogId;
    }

    public ChangeType getType() {
        return type;
    }

    public void setType(ChangeType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}