package org.xhy.community.infrastructure.exception;

public class SystemException extends BaseException {
    
    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public SystemException(ErrorCode errorCode, String customMessage) {
        super(errorCode, customMessage);
    }
    
    public SystemException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
    
    public SystemException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode, customMessage, cause);
    }
}