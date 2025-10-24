package org.xhy.community.infrastructure.exception;

/**
 * 聊天室模块错误码
 */
public enum ChatErrorCode implements ErrorCode {
    ROOM_NOT_FOUND(8200, "房间不存在"),
    UNAUTHORIZED_ROOM_ACCESS(8201, "无权访问该房间"),
    INVALID_PLAN_FOR_USER(8202, "普通用户不能指定该套餐"),
    MESSAGE_NOT_FOUND(8203, "消息不存在"),
    QUOTE_CROSS_ROOM_NOT_ALLOWED(8204, "禁止跨房间引用消息"),
    SENDER_NOT_MEMBER(8205, "非成员无法发送消息"),
    OWNER_CANNOT_LEAVE(8206, "房主不能退出房间"),
    PLAN_PERMISSION_DENIED(8207, "无权限加入该房间（套餐不匹配）"),
    ROOM_DELETE_ONLY_OWNER(8208, "仅房主可删除房间");

    private final int code;
    private final String message;

    ChatErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
