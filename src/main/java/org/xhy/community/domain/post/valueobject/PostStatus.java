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
}