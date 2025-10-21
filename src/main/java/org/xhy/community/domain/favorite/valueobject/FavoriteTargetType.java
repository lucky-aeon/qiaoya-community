package org.xhy.community.domain.favorite.valueobject;

public enum FavoriteTargetType {
    POST("文章"),
    CHAPTER("章节"),
    COMMENT("评论"),
    INTERVIEW_QUESTION("题目");

    private final String description;

    FavoriteTargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static FavoriteTargetType fromCode(String code) {
        for (FavoriteTargetType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown favorite target type: " + code);
    }
}
