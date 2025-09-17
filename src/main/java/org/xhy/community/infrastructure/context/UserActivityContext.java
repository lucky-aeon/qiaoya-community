package org.xhy.community.infrastructure.context;

/**
 * 用户活动上下文
 * 封装当前请求的用户活动相关信息
 */
public class UserActivityContext {
    
    private String ip;
    private String userAgent;
    private String browser;
    private String equipment;
    
    public UserActivityContext(String ip, String userAgent, String browser, String equipment) {
        this.ip = ip;
        this.userAgent = userAgent;
        this.browser = browser;
        this.equipment = equipment;
    }
    
    public String getIp() {
        return ip;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public String getBrowser() {
        return browser;
    }
    
    public String getEquipment() {
        return equipment;
    }
}