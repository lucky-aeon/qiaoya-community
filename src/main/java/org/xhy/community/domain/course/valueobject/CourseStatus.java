package org.xhy.community.domain.course.valueobject;

public enum CourseStatus {
    PENDING("待更新"),
    IN_PROGRESS("更新中"),
    COMPLETED("已完成");
    
    private final String description;
    
    CourseStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static CourseStatus fromCode(String code) {
        for (CourseStatus status : values()) {
            if (status.name().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown course status code: " + code);
    }
}