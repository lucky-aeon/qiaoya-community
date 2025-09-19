package org.xhy.community.infrastructure.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标识 List 字段中元素的类型
 * 用于 UniversalListConverter 进行正确的类型反序列化
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ListElementType {
    /**
     * List中元素的具体类型
     */
    Class<?> value();
}