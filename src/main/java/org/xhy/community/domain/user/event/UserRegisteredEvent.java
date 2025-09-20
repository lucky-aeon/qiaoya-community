package org.xhy.community.domain.user.event;

import org.springframework.context.ApplicationEvent;

/**
 * 用户注册事件
 */
public class UserRegisteredEvent extends ApplicationEvent {

    private final String userId;
    private final String email;

    public UserRegisteredEvent(Object source, String userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}