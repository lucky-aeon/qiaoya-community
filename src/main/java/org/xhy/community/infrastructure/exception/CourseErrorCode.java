package org.xhy.community.infrastructure.exception;

public enum CourseErrorCode implements ErrorCode {
    
    COURSE_NOT_FOUND(5000, "课程不存在"),
    COURSE_RATING_INVALID(5001, "评分必须在0.00-5.00之间"),
    CHAPTER_NOT_FOUND(5002, "章节不存在"),
    CHAPTER_ACCESS_DENIED(5003, "章节未解锁，无权限访问"),
    COURSE_ACCESS_DENIED(5004, "课程未解锁，无权限访问");
    
    private final int code;
    private final String message;
    
    CourseErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
    @Override
    public int getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
