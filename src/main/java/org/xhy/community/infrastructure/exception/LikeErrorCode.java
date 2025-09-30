package org.xhy.community.infrastructure.exception;

/**
 * 点赞相关错误码
 */
public enum LikeErrorCode implements ErrorCode {

    ALREADY_LIKED(7000, "已经点赞过了"),
    NOT_LIKED(7001, "尚未点赞"),
    LIKE_CREATE_FAILED(7002, "点赞创建失败"),
    LIKE_UPDATE_FAILED(7003, "点赞状态更新失败"),
    TARGET_NOT_FOUND(7004, "点赞目标不存在"),
    INVALID_TARGET_TYPE(7005, "无效的点赞目标类型");

    private final int code;
    private final String message;

    LikeErrorCode(int code, String message) {
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