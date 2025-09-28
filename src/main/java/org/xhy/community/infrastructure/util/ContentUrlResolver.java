package org.xhy.community.infrastructure.util;

import org.springframework.stereotype.Component;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;

/**
 * 统一的内容路径解析器（相对路径）
 * 仅负责返回前端路由相对路径，域名拼接由 WebUrlConfig 处理
 */
@Component
public class ContentUrlResolver {

    /**
     * 根据内容类型与ID返回相对路径
     */
    public String contentPath(ContentType type, String id) {
        if (type == null || id == null) return null;
        return switch (type) {
            case POST -> "/dashboard/discussions/" + id;
            case COURSE -> "/dashboard/courses/" + id;
            case CHAPTER -> null; // 章节需要courseId与chapterId，请使用 chapterPath()
            case COMMENT -> "/dashboard/discussions/" + id; // 评论通常跳转到所在页面，兜底
        };
    }

    /**
     * 根据关注目标类型与ID返回相对路径
     */
    public String targetPath(FollowTargetType type, String id) {
        if (type == null || id == null) return null;
        return switch (type) {
            case POST -> "/dashboard/discussions/" + id;
            case COURSE -> "/dashboard/courses/" + id;
            case CHAPTER -> "/dashboard/chapters/" + id; // 具体章节仍建议用 chapterPath
            case USER -> "/dashboard/users/" + id;
        };
    }

    /**
     * 章节相对路径
     */
    public String chapterPath(String courseId, String chapterId) {
        if (courseId == null || chapterId == null) return null;
        return "/dashboard/courses/" + courseId + "/chapters/" + chapterId;
    }
}
