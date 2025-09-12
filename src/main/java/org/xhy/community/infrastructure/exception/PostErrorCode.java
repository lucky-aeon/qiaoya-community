package org.xhy.community.infrastructure.exception;

public enum PostErrorCode implements ErrorCode {
    
    POST_NOT_FOUND(4000, "文章不存在"),
    POST_ALREADY_PUBLISHED(4001, "文章已发布"),
    UNAUTHORIZED_EDIT(4002, "只能编辑自己的文章"),
    UNAUTHORIZED_PUBLISH(4003, "只能发布自己的文章"),
    CATEGORY_NOT_FOUND(4004, "分类不存在"),
    CATEGORY_NAME_EXISTS(4005, "分类名称已存在"),
    CATEGORY_DISABLED(4006, "分类已禁用"),
    POST_ALREADY_LIKED(4007, "您已点赞过该文章"),
    POST_NOT_LIKED(4008, "您还未点赞该文章"),
    POST_ALREADY_DRAFT(4009, "文章已是草稿状态"),
    POST_NOT_PUBLISHED(4010, "只有已发布的文章才能置顶"),
    PARENT_CATEGORY_NOT_FOUND(4011, "父分类不存在"),
    CATEGORY_TYPE_MISMATCH(4012, "子分类类型必须与父分类类型一致"),
    CATEGORY_NOT_EXISTS(4013, "分类不存在");
    
    private final int code;
    private final String message;
    
    PostErrorCode(int code, String message) {
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