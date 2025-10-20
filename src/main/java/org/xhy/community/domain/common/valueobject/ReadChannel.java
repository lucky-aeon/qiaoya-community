package org.xhy.community.domain.common.valueobject;

/**
 * 导航未读数统计的频道维度。
 * POSTS：文章
 * QUESTIONS：面试题/题目
 */
public enum ReadChannel {
    POSTS("文章"),
    QUESTIONS("题目"),
    CHAPTERS("课程章节");

    private final String description;

    ReadChannel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ReadChannel fromCode(String code) {
        for (ReadChannel c : values()) {
            if (c.name().equals(code)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown ReadChannel code: " + code);
    }
}
