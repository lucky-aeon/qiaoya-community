package org.xhy.community.application.updatelog.dto;

import org.xhy.community.domain.updatelog.valueobject.UpdateLogStatus;

import java.time.LocalDateTime;
import java.util.List;

public class UpdateLogDTO {

    private String id;
    private String version;
    private String title;
    private String description;
    private String authorId;
    private String authorName;
    private UpdateLogStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<UpdateLogChangeDTO> changes;

    public UpdateLogDTO() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public UpdateLogStatus getStatus() {
        return status;
    }

    public void setStatus(UpdateLogStatus status) {
        this.status = status;
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

    public List<UpdateLogChangeDTO> getChanges() {
        return changes;
    }

    public void setChanges(List<UpdateLogChangeDTO> changes) {
        this.changes = changes;
    }
}