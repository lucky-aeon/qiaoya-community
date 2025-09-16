package org.xhy.community.domain.common.valueobject;

/**
 * 访问权限级别枚举
 * 用于区分不同用户角色的数据访问权限
 */
public enum AccessLevel {
    
    /** 普通用户权限 - 只能访问自己的数据 */
    USER,
    
    /** 管理员权限 - 可以访问所有数据 */
    ADMIN
}