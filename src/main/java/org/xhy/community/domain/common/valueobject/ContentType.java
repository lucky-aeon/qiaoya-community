package org.xhy.community.domain.common.valueobject;

/**
 * 内容类型枚举
 * 用于标识不同类型的可发布内容
 */
public enum ContentType {

    POST("文章"),
    COURSE("课程"),
    CHAPTER("章节"),
    COMMENT("评论"),
    UPDATE_LOG("更新日志"),
    INTERVIEW_QUESTION("题目"),
    PUBLISH_CONTENT("关注内容");


    private final String description;

    ContentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
