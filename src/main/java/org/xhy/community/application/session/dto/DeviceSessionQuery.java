package org.xhy.community.application.session.dto;

import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 设备会话查询对象（管理员使用）
 */
public class DeviceSessionQuery extends PageRequest {
    /** 可选：筛选特定用户 */
    private String userId;
    /** 可选：按用户名搜索 */
    private String username;
    /** 可选：按IP搜索 */
    private String ip;

    public DeviceSessionQuery() {
        super();
    }

    public DeviceSessionQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

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