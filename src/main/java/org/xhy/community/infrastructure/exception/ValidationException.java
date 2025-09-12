package org.xhy.community.infrastructure.exception;

public class ValidationException extends BaseException {
    
    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ValidationException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public ValidationException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public ValidationException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode, customMessage, cause);
    }
}