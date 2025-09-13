package org.xhy.community.infrastructure.exception;

public enum CommentErrorCode implements ErrorCode {
    
    COMMENT_NOT_FOUND(5000, "评论不存在"),
    UNAUTHORIZED_DELETE(5001, "只能删除自己的评论"),
    PARENT_COMMENT_NOT_FOUND(5002, "父评论不存在"),
    COMMENT_CONTENT_EMPTY(5003, "评论内容不能为空"),
    BUSINESS_NOT_FOUND(5004, "关联的业务对象不存在"),
    COMMENT_ALREADY_LIKED(5005, "您已点赞过该评论"),
    COMMENT_NOT_LIKED(5006, "您还未点赞该评论"),
    REPLY_TO_SELF(5007, "不能回复自己的评论"),
    COMMENT_TOO_LONG(5008, "评论内容过长"),
    COMMENT_DELETED(5009, "评论已被删除");
    
    private final int code;
    private final String message;
    
    CommentErrorCode(int code, String message) {
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