package org.xhy.community.interfaces.ops.request;

import org.xhy.community.interfaces.common.request.PageRequest;

public class BackupQueryRequest extends PageRequest {
    private String status; // SUCCESS / FAILED 可选

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

