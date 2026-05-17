package org.xhy.community.interfaces.course.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ArchiveRequest {

    @NotBlank(message = "归档原因不能为空")
    @Size(max = 1000, message = "归档原因不能超过1000字符")
    private String reason;

    public ArchiveRequest() {
    }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
