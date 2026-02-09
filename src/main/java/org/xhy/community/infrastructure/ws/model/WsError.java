package org.xhy.community.infrastructure.ws.model;

/**
 * 错误帧负载
 * code：错误码（如 UNKNOWN_TYPE / BAD_REQUEST / UNAUTHORIZED_ROOM_ACCESS 等）
 * message：人类可读的错误描述
 */
public class WsError {
    private String code;
    private String message;

    public WsError() {}

    public WsError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
