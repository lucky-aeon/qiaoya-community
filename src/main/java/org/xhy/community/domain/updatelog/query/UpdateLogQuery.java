package org.xhy.community.domain.updatelog.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.updatelog.valueobject.UpdateLogStatus;

public class UpdateLogQuery extends BasePageQuery {

    private UpdateLogStatus status;
    private String version;
    private String title;

    public UpdateLogQuery() {}

    public UpdateLogQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    public UpdateLogStatus getStatus() {
        return status;
    }

    public void setStatus(UpdateLogStatus status) {
        this.status = status;
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
}

