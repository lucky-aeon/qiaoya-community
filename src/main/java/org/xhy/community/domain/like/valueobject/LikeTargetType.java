package org.xhy.community.domain.like.valueobject;

public enum LikeTargetType {
    COURSE("课程"),
    POST("文章"),
    CHAPTER("章节"),
    COMMENT("评论"),
    INTERVIEW_QUESTION("面试题");

    private final String description;

    LikeTargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static LikeTargetType fromCode(String code) {
        for (LikeTargetType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown like target type code: " + code);
    }
}
