package org.xhy.community.domain.user.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.user.valueobject.UserStatus;

public class UserQuery extends BasePageQuery {
    
    private UserStatus status;
    private String name;
    private String email;

    public UserQuery() {
    }
    
    public UserQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
}