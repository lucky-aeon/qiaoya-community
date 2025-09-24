package org.xhy.community.domain.user.event;

import org.springframework.context.ApplicationEvent;

/**
 * 用户登录事件
 */
public class UserLoginEvent extends ApplicationEvent {

    private final String userId;
    private final String email;
    private final String ip;

    public UserLoginEvent(Object source, String userId, String email, String ip) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.ip = ip;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getIp() {
        return ip;
    }
}