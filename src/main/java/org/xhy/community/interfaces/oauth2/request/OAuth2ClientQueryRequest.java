package org.xhy.community.interfaces.oauth2.request;

import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * OAuth2客户端查询请求对象
 * 继承分页请求，支持分页查询
 */
public class OAuth2ClientQueryRequest extends PageRequest {

    /** 客户端名称（模糊查询） */
    private String clientName;

    /** 客户端状态 */
    private String status;

    public OAuth2ClientQueryRequest() {
        super();
    }

    public OAuth2ClientQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    // Getters and Setters

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
