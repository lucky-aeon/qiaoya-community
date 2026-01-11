package org.xhy.community.infrastructure.exception;

/**
 * Codex 集成相关错误码
 */
public enum CodexErrorCode implements ErrorCode {
    CODEX_CONFIG_NOT_FOUND(7001, "Codex 未配置"),
    CODEX_DISABLED(7002, "Codex 功能已禁用"),
    CODEX_UNAUTHORIZED(7003, "Codex 授权失败"),
    CODEX_FETCH_FAILED(7004, "Codex 接口调用失败");

    private final int code;
    private final String message;

    CodexErrorCode(int code, String message) {
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

