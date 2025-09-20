package org.xhy.community.domain.updatelog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.updatelog.valueobject.UpdateLogStatus;

@TableName("update_logs")
public class UpdateLogEntity extends BaseEntity {

    private String version;
    private String title;
    private String description;
    private String authorId;
    private UpdateLogStatus status;

    public UpdateLogEntity() {}

    public UpdateLogEntity(String version, String title, String description, String authorId) {
        this.version = version;
        this.title = title;
        this.description = description;
        this.authorId = authorId;
        this.status = UpdateLogStatus.DRAFT;
    }

    public void publish() {
        this.status = UpdateLogStatus.PUBLISHED;
    }

    public void draft() {
        this.status = UpdateLogStatus.DRAFT;
    }

    public boolean isPublished() {
        return UpdateLogStatus.PUBLISHED.equals(this.status);
    }

    public boolean isDraft() {
        return UpdateLogStatus.DRAFT.equals(this.status);
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


    public UpdateLogStatus getStatus() {
        return status;
    }

    public void setStatus(UpdateLogStatus status) {
        this.status = status;
    }
}