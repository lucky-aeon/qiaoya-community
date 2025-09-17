package org.xhy.community.domain.common.valueobject;

/**
 * 用户活动类型枚举
 * 用于记录用户在系统中进行的各种活动
 */
public enum ActivityType {
    
    /**
     * 登录成功
     */
    LOGIN_SUCCESS("登录成功"),
    
    /**
     * 登录失败
     */
    LOGIN_FAILED("登录失败"),
    
    /**
     * 注册成功
     */
    REGISTER_SUCCESS("注册成功"),
    
    /**
     * 注册失败
     */
    REGISTER_FAILED("注册失败"),
    
    /**
     * 用户登出
     */
    LOGOUT("用户登出"),
    
    /**
     * 修改密码
     */
    CHANGE_PASSWORD("修改密码"),
    
    /**
     * 重置密码
     */
    RESET_PASSWORD("重置密码");
    
    private final String description;
    
    ActivityType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 枚举代码
     * @return 对应的枚举值，如果不存在则抛出异常
     */
    public static ActivityType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        try {
            return ActivityType.valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的活动类型: " + code);
        }
    }
}