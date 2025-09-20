package org.xhy.community.interfaces.updatelog.request;

import org.xhy.community.domain.updatelog.valueobject.UpdateLogStatus;
import org.xhy.community.interfaces.common.request.PageRequest;

public class AdminUpdateLogQueryRequest extends PageRequest {

    private UpdateLogStatus status;
    private String version;
    private String title;

    public AdminUpdateLogQueryRequest() {
        super();
    }

    public AdminUpdateLogQueryRequest(Integer pageNum, Integer pageSize) {
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