package org.xhy.community.infrastructure.annotation;

import java.lang.annotation.*;

/**
 * 前台接口功能权限校验注解（基于套餐权限码）
 * 用法：
 * 1) 仅代码（向后兼容）：
 *    @RequiresPlanPermissions({"RESOURCE_DOWNLOAD", "COURSE_VIEW_PREMIUM"})
 * 2) 代码 + 中文名（推荐）：
 *    @RequiresPlanPermissions(items = {
 *        @RequiresPlanPermissions.Item(code = "RESOURCE_DOWNLOAD", name = "下载资源")
 *    })
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPlanPermissions {
    /** 仅代码（向后兼容） */
    String[] value() default {};

    /** 代码 + 中文名（推荐） */
    Item[] items() default {};

    @interface Item {
        String code();
        String name();
    }
}
