package org.xhy.community.domain.auth.query;

import java.time.LocalDateTime;
import org.xhy.community.domain.common.valueobject.AuthProvider;

public class UserSocialAccountQuery {
    private Integer pageNum;
    private Integer pageSize;
    private String userId;
    private AuthProvider provider;
    private String login;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public AuthProvider getProvider() { return provider; }
    public void setProvider(AuthProvider provider) { this.provider = provider; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
}

