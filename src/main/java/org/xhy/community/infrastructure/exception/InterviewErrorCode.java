package org.xhy.community.infrastructure.exception;

public enum InterviewErrorCode implements ErrorCode {

    INTERVIEW_QUESTION_NOT_FOUND(9000, "面试题不存在"),
    TITLE_ALREADY_EXISTS(9001, "同分类下题目标题已存在"),
    CATEGORY_NOT_FOUND(9002, "分类不存在"),
    CATEGORY_TYPE_MISMATCH(9003, "分类类型不匹配"),
    CATEGORY_DISABLED(9004, "分类已禁用"),
    ALREADY_PUBLISHED(9005, "题目已发布"),
    UNAUTHORIZED_EDIT(9006, "只能编辑自己的题目"),
    INVALID_RATING(9007, "评分必须在1到5之间");

    private final int code;
    private final String message;

    InterviewErrorCode(int code, String message) {
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
