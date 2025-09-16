package org.xhy.community.interfaces.user.request;

import org.xhy.community.domain.user.valueobject.UserStatus;
import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 管理员用户查询请求
 * 管理员查询所有用户列表，支持邮箱、昵称、状态条件查询和分页
 */
public class AdminUserQueryRequest extends PageRequest {
    
    /** 用户邮箱，模糊查询 */
    private String email;
    
    /** 用户昵称，模糊查询 */
    private String name;
    
    /** 用户状态，精确查询 */
    private UserStatus status;
    
    public AdminUserQueryRequest() {
    }
    
    public AdminUserQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public void setStatus(UserStatus status) {
        this.status = status;
    }
}