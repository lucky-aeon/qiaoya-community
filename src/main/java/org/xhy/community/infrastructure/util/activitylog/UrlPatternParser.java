package org.xhy.community.infrastructure.util.activitylog;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL模式解析器
 * 自动从请求路径中解析目标类型和目标ID
 * 支持多种URL模式的配置化解析
 */
public class UrlPatternParser {
    
    /**
     * URL模式映射配置
     * 根据正则表达式匹配URL并提取目标信息
     */
    private static final Map<Pattern, TargetTypeMapping> URL_PATTERNS;
    
    static {
        URL_PATTERNS = new HashMap<>();
        
        // 文章相关
        URL_PATTERNS.put(Pattern.compile("/api/posts/(\\w+)"), new TargetTypeMapping("POST", 1));
        URL_PATTERNS.put(Pattern.compile("/api/admin/posts/(\\w+)"), new TargetTypeMapping("POST", 1));
        URL_PATTERNS.put(Pattern.compile("/api/app/posts/(\\w+)"), new TargetTypeMapping("POST", 1));
        
        // 课程相关
        URL_PATTERNS.put(Pattern.compile("/api/courses/(\\w+)"), new TargetTypeMapping("COURSE", 1));
        URL_PATTERNS.put(Pattern.compile("/api/admin/courses/(\\w+)"), new TargetTypeMapping("COURSE", 1));
        
        // 用户相关
        URL_PATTERNS.put(Pattern.compile("/api/users/(\\w+)"), new TargetTypeMapping("USER", 1));
        URL_PATTERNS.put(Pattern.compile("/api/admin/users/(\\w+)"), new TargetTypeMapping("USER", 1));
        
        // 章节相关（章节ID在第二个组）
        URL_PATTERNS.put(Pattern.compile("/api/courses/(\\w+)/chapters/(\\w+)"), new TargetTypeMapping("CHAPTER", 2));
        
        // 评论相关
        URL_PATTERNS.put(Pattern.compile("/api/posts/(\\w+)/comments/(\\w+)"), new TargetTypeMapping("COMMENT", 2));
        
        // 点赞相关
        URL_PATTERNS.put(Pattern.compile("/api/posts/(\\w+)/like"), new TargetTypeMapping("POST", 1));
        
        // 关注相关
        URL_PATTERNS.put(Pattern.compile("/api/users/(\\w+)/follow"), new TargetTypeMapping("USER", 1));
        
        // 课程注册相关
        URL_PATTERNS.put(Pattern.compile("/api/courses/(\\w+)/enroll"), new TargetTypeMapping("COURSE", 1));

        // 公开资源访问
        URL_PATTERNS.put(Pattern.compile("/api/public/resource/([\\w-]+)/access"), new TargetTypeMapping("RESOURCE", 1));

        // 管理员分类/章节/CDK/更新日志
        URL_PATTERNS.put(Pattern.compile("/api/admin/categories/([\\w-]+)"), new TargetTypeMapping("CATEGORY", 1));
        URL_PATTERNS.put(Pattern.compile("/api/admin/chapters/([\\w-]+)"), new TargetTypeMapping("CHAPTER", 1));
        URL_PATTERNS.put(Pattern.compile("/api/admin/cdk/([\\w-]+)"), new TargetTypeMapping("CDK", 1));
        URL_PATTERNS.put(Pattern.compile("/api/admin/update-logs/([\\w-]+)(?:/toggle-status)?"), new TargetTypeMapping("UPDATE_LOG", 1));
    }
    
    /**
     * 从URL路径解析目标信息
     * 
     * @param requestPath 请求路径
     * @return 解析出的目标信息，如果无法解析则返回null
     */
    public static TargetInfo parseFromUrl(String requestPath) {
        if (requestPath == null || requestPath.trim().isEmpty()) {
            return null;
        }
        
        // 遍历所有配置的URL模式
        for (Map.Entry<Pattern, TargetTypeMapping> entry : URL_PATTERNS.entrySet()) {
            Pattern pattern = entry.getKey();
            Matcher matcher = pattern.matcher(requestPath);
            
            if (matcher.matches()) {
                TargetTypeMapping mapping = entry.getValue();
                
                // 检查组索引是否有效
                if (mapping.getIdGroupIndex() <= matcher.groupCount()) {
                    String targetId = matcher.group(mapping.getIdGroupIndex());
                    return new TargetInfo(mapping.getTargetType(), targetId);
                }
            }
        }
        
        // 如果无法解析，返回null（如POST /api/posts创建操作）
        return null;
    }
    
    /**
     * 检查URL路径是否可以解析
     * 
     * @param requestPath 请求路径
     * @return 如果可以解析返回true，否则返回false
     */
    public static boolean canParse(String requestPath) {
        return parseFromUrl(requestPath) != null;
    }
}
