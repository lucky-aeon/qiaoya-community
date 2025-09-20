package org.xhy.community.interfaces.updatelog.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.xhy.community.domain.updatelog.valueobject.ChangeType;

public class CreateChangeRequest {

    @NotNull(message = "变更类型不能为空")
    private ChangeType type;

    @NotBlank(message = "变更标题不能为空")
    private String title;

    private String description;

    private String category;

    private Integer sortOrder;

    public CreateChangeRequest() {}

    public CreateChangeRequest(ChangeType type, String title, String description, String category, Integer sortOrder) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.category = category;
        this.sortOrder = sortOrder;
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