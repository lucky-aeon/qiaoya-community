package org.xhy.community.infrastructure.exception;

/**
 * 通知错误码
 */
public enum NotificationErrorCode implements ErrorCode {
    
    NOTIFICATION_NOT_FOUND(5001, "通知不存在"),
    NOTIFICATION_ACCESS_DENIED(5002, "无权限访问该通知"),
    NOTIFICATION_SEND_FAILED(5003, "通知发送失败"),
    NOTIFICATION_TEMPLATE_NOT_FOUND(5004, "通知模板不存在"),
    NOTIFICATION_CHANNEL_UNAVAILABLE(5005, "通知渠道不可用"),
    INVALID_NOTIFICATION_STATUS(5006, "无效的通知状态");
    
    private final int code;
    private final String message;
    
    NotificationErrorCode(int code, String message) {
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