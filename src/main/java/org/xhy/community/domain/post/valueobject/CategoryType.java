package org.xhy.community.domain.post.valueobject;

public enum CategoryType {
    ARTICLE("文章"),
    QA("问答");
    
    private final String description;
    
    CategoryType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static CategoryType fromCode(String code) {
        for (CategoryType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown category type code: " + code);
    }
}