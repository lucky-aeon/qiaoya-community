package org.xhy.community.infrastructure.exception;

/**
 * 关注相关错误码
 */
public enum FollowErrorCode implements ErrorCode {
    
    CANNOT_FOLLOW_SELF(6000, "不能关注自己"),
    ALREADY_FOLLOWED(6001, "已经关注过了"),
    NOT_FOLLOWED(6002, "尚未关注"),
    FOLLOW_CREATE_FAILED(6003, "关注创建失败"),
    FOLLOW_UPDATE_FAILED(6004, "关注状态更新失败"),
    TARGET_NOT_FOUND(6005, "关注目标不存在"),
    INVALID_TARGET_TYPE(6006, "无效的关注目标类型"),
    FOLLOW_LIMIT_EXCEEDED(6007, "关注数量超出限制");
    
    private final int code;
    private final String message;
    
    FollowErrorCode(int code, String message) {
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