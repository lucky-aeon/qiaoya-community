package org.xhy.community.interfaces.ainews.request;

import jakarta.validation.constraints.Pattern;
import org.xhy.community.domain.ainews.valueobject.DailyItemStatus;
import org.xhy.community.interfaces.common.request.PageRequest;

public class AdminDailyQueryRequest extends PageRequest {

    @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "日期格式应为YYYY-MM-DD")
    private String date; // 可为空

    private DailyItemStatus status; // 可为空

    private Boolean withContent = Boolean.FALSE;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public DailyItemStatus getStatus() { return status; }
    public void setStatus(DailyItemStatus status) { this.status = status; }
    public Boolean getWithContent() { return withContent; }
    public void setWithContent(Boolean withContent) { this.withContent = withContent; }
}

