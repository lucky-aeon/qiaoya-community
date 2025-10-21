package org.xhy.community.infrastructure.exception;

/**
 * 收藏相关错误码
 */
public enum FavoriteErrorCode implements ErrorCode {

    ALREADY_FAVORITED(8000, "已经收藏过了"),
    NOT_FAVORITED(8001, "尚未收藏"),
    FAVORITE_CREATE_FAILED(8002, "收藏创建失败"),
    FAVORITE_UPDATE_FAILED(8003, "收藏状态更新失败"),
    TARGET_NOT_FOUND(8004, "收藏目标不存在"),
    INVALID_TARGET_TYPE(8005, "无效的收藏目标类型");

    private final int code;
    private final String message;

    FavoriteErrorCode(int code, String message) {
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
