package org.xhy.community.interfaces.user.request;

import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 黑名单用户查询请求
 * 管理员查询被拉黑用户列表，支持用户名、邮箱条件查询和分页
 */
public class BlacklistQueryRequest extends PageRequest {

    /** 用户名，模糊查询 */
    private String username;

    /** 用户邮箱，模糊查询 */
    private String email;

    public BlacklistQueryRequest() {
        super();
    }

    public BlacklistQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}