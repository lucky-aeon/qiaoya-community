package org.xhy.community.infrastructure.annotation;

import org.xhy.community.domain.common.valueobject.ActivityType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务活动日志注解
 * 用于记录用户的业务操作行为，如查看文章、发表内容等
 * 采用极简设计，自动从URL解析目标类型和ID
 * 
 * <p>使用示例：
 * <pre>
 * {@code
 * @GetMapping("/{postId}")
 * @ActivityLog(ActivityType.VIEW_POST)  // 极简！自动解析 targetType="POST", targetId=postId
 * public ApiResponse<PostDTO> getPostById(@PathVariable String postId) {
 *     return ApiResponse.success(postAppService.getPostById(postId));
 * }
 * 
 * @PostMapping
 * @ActivityLog(ActivityType.CREATE_POST)  // 创建操作，无目标ID
 * public ApiResponse<PostDTO> createPost(@RequestBody CreatePostRequest request) {
 *     return ApiResponse.success(postAppService.createPost(request));
 * }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ActivityLog {
    
    /**
     * 活动类型（必填）
     * 
     * @return 活动类型枚举值
     */
    ActivityType value();
    
    /**
     * 是否异步处理，默认true
     * 异步处理可以避免日志记录影响主业务性能
     * 
     * @return true表示异步处理，false表示同步处理
     */
    boolean async() default true;
    
    /**
     * 是否记录请求体，默认true
     * 对于包含敏感信息的请求，可以设置为false
     * 
     * @return true表示记录请求体，false表示不记录
     */
    boolean recordRequest() default true;
}