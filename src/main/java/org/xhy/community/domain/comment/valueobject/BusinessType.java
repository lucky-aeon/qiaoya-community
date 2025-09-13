package org.xhy.community.domain.comment.valueobject;

public enum BusinessType {
    POST("文章"),
    COURSE("课程");
    
    private final String description;
    
    BusinessType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static BusinessType fromCode(String code) {
        for (BusinessType type : values()) {
            if (type.name().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown business type code: " + code);
    }
}