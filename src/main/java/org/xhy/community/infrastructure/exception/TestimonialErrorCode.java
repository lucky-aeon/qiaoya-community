package org.xhy.community.infrastructure.exception;

public enum TestimonialErrorCode implements ErrorCode {

    TESTIMONIAL_NOT_FOUND(6000, "学员评价不存在"),
    USER_ALREADY_SUBMITTED(6001, "您已提交过评价，每个用户只能提交一条评价"),
    TESTIMONIAL_NOT_MODIFIABLE(6002, "只有待审核或已拒绝状态的评价才能修改"),
    UNAUTHORIZED_MODIFY(6003, "只能修改自己的评价"),
    INVALID_RATING(6004, "评分必须在1-5分之间"),
    CONTENT_EMPTY(6005, "评价内容不能为空"),
    INVALID_STATUS_TRANSITION(6006, "无效的状态变更"),
    TESTIMONIAL_CONTENT_TOO_LONG(6007, "评价内容过长");

    private final int code;
    private final String message;

    TestimonialErrorCode(int code, String message) {
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