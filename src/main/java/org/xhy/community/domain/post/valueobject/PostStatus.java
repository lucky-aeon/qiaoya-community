package org.xhy.community.domain.post.valueobject;

public enum PostStatus {
    DRAFT("草稿"),
    PUBLISHED("已发布");
    
    private final String description;
    
    PostStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static PostStatus fromCode(String code) {
        for (PostStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown post status code: " + code);
    }
}