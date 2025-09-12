package org.xhy.community.infrastructure.config;

import org.xhy.community.infrastructure.exception.ErrorCode;

public enum ValidationErrorCode implements ErrorCode {
    
    PARAM_NULL(2000, "参数不能为空"),
    PARAM_INVALID(2001, "参数格式错误"),
    EMAIL_FORMAT_INVALID(2002, "邮箱格式错误"),
    PASSWORD_TOO_SHORT(2003, "密码长度不能少于6位"),
    PASSWORD_TOO_LONG(2004, "密码长度不能超过20位"),
    NAME_TOO_SHORT(2005, "姓名长度不能少于2位"),
    NAME_TOO_LONG(2006, "姓名长度不能超过50位"),
    TITLE_TOO_SHORT(2007, "标题长度不能少于5位"),
    TITLE_TOO_LONG(2008, "标题长度不能超过200位"),
    CONTENT_TOO_SHORT(2009, "内容长度不能少于10位");
    
    private final int code;
    private final String message;
    
    ValidationErrorCode(int code, String message) {
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