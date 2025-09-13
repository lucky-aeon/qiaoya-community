package org.xhy.community.infrastructure.exception;

public enum ResourceErrorCode implements ErrorCode {
    
    RESOURCE_NOT_FOUND(5000, "资源不存在"),
    UPLOAD_FAILED(5001, "文件上传失败"),
    DELETE_FAILED(5002, "文件删除失败"),
    FILE_NOT_FOUND(5003, "文件未找到"),
    ACCESS_DENIED(5004, "无权访问此资源"),
    FILE_SIZE_EXCEEDED(5005, "文件大小超出限制"),
    INVALID_FILE_TYPE(5006, "不支持的文件类型"),
    PRESIGNED_URL_GENERATION_FAILED(5007, "生成预签名URL失败");
    
    private final int code;
    private final String message;
    
    ResourceErrorCode(int code, String message) {
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