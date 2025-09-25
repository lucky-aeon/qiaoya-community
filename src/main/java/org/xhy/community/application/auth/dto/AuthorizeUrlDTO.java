package org.xhy.community.application.auth.dto;

public class AuthorizeUrlDTO {
    private String url;
    private String state;
    private Long expireAt;

    public AuthorizeUrlDTO() {}
    public AuthorizeUrlDTO(String url, String state, Long expireAt) {
        this.url = url; this.state = state; this.expireAt = expireAt;
    }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Long getExpireAt() { return expireAt; }
    public void setExpireAt(Long expireAt) { this.expireAt = expireAt; }
}

