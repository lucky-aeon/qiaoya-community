package org.xhy.community.interfaces.user.request;

import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 管理员设备会话查询请求
 * 管理员查询所有用户的设备会话信息，支持多条件过滤和分页
 */
public class DeviceSessionQueryRequest extends PageRequest {

    /** 可选：筛选特定用户 */
    private String userId;

    /** 可选：按用户名搜索 */
    private String username;

    /** 可选：按IP搜索 */
    private String ip;

    public DeviceSessionQueryRequest() {
        super();
    }

    public DeviceSessionQueryRequest(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}