package org.xhy.community.interfaces.updatelog.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateUpdateLogRequest {

    @NotBlank(message = "版本号不能为空")
    @Size(max = 50, message = "版本号长度不能超过50字符")
    private String version;

    @NotBlank(message = "更新标题不能为空")
    @Size(max = 200, message = "更新标题长度不能超过200字符")
    private String title;

    private String description;

    @Valid
    private List<CreateChangeRequest> changes;

    public CreateUpdateLogRequest() {}

    public CreateUpdateLogRequest(String version, String title, String description, List<CreateChangeRequest> changes) {
        this.version = version;
        this.title = title;
        this.description = description;
        this.changes = changes;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public List<CreateChangeRequest> getChanges() {
        return changes;
    }

    public void setChanges(List<CreateChangeRequest> changes) {
        this.changes = changes;
    }
}