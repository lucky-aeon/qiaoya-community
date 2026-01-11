package org.xhy.community.infrastructure.exception;

/**
 * 点赞相关错误码
 */
public enum LikeErrorCode implements ErrorCode {

    ALREADY_LIKED(7400, "已经点赞过了"),
    NOT_LIKED(7401, "尚未点赞"),
    LIKE_CREATE_FAILED(7402, "点赞创建失败"),
    LIKE_UPDATE_FAILED(7403, "点赞状态更新失败"),
    TARGET_NOT_FOUND(7404, "点赞目标不存在"),
    INVALID_TARGET_TYPE(7405, "无效的点赞目标类型");

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
