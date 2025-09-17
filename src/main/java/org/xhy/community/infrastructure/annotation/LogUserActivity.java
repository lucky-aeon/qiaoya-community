package org.xhy.community.infrastructure.annotation;

import org.xhy.community.domain.common.valueobject.ActivityType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户活动日志注解
 * 用于标记需要记录用户活动日志的方法
 * 通过AOP切面自动记录用户的登录、注册等行为
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogUserActivity {
    
    /**
     * 成功时记录的活动类型
     */
    ActivityType successType();
    
    /**
     * 失败时记录的活动类型
     */
    ActivityType failureType();
    
    /**
     * 是否记录成功日志，默认true
     */
    boolean logSuccess() default true;
    
    /**
     * 是否记录失败日志，默认true
     */
    boolean logFailure() default true;
}