package org.xhy.community.domain.common.valueobject;

/**
 * 用户活动类型枚举
 * 用于记录用户在系统中进行的各种活动
 */
public enum ActivityType {
    
    // ==================== 认证相关（现有，保持不变） ====================
    /**
     * 登录成功
     */
    LOGIN_SUCCESS("登录成功"),
    
    /**
     * 登录失败
     */
    LOGIN_FAILED("登录失败"),
    
    /**
     * 注册成功
     */
    REGISTER_SUCCESS("注册成功"),
    
    /**
     * 注册失败
     */
    REGISTER_FAILED("注册失败"),
    
    /**
     * 用户登出
     */
    LOGOUT("用户登出"),
    
    /**
     * 修改密码
     */
    CHANGE_PASSWORD("修改密码"),
    
    /**
     * 重置密码
     */
    RESET_PASSWORD("重置密码"),
    
    // ==================== 内容浏览 ====================
    /**
     * 查看文章
     */
    VIEW_POST("查看文章"),
    
    /**
     * 查看课程
     */
    VIEW_COURSE("查看课程"),
    
    /**
     * 查看用户资料
     */
    VIEW_USER_PROFILE("查看用户资料"),
    
    /**
     * 搜索内容
     */
    SEARCH_CONTENT("搜索内容"),
    
    // ==================== 内容创作 ====================
    /**
     * 发表文章
     */
    CREATE_POST("发表文章"),
    
    /**
     * 编辑文章
     */
    UPDATE_POST("编辑文章"),
    
    /**
     * 删除文章
     */
    DELETE_POST("删除文章"),
    
    /**
     * 创建课程
     */
    CREATE_COURSE("创建课程"),
    
    /**
     * 编辑课程
     */
    UPDATE_COURSE("编辑课程"),
    
    /**
     * 删除课程
     */
    DELETE_COURSE("删除课程"),
    
    // ==================== 社交互动 ====================
    /**
     * 点赞文章
     */
    LIKE_POST("点赞文章"),
    
    /**
     * 取消点赞文章
     */
    UNLIKE_POST("取消点赞文章"),
    
    /**
     * 评论文章
     */
    COMMENT_POST("评论文章"),
    
    /**
     * 删除评论
     */
    DELETE_COMMENT("删除评论"),
    
    /**
     * 关注用户
     */
    FOLLOW_USER("关注用户"),
    
    /**
     * 取消关注用户
     */
    UNFOLLOW_USER("取消关注用户"),
    
    /**
     * 分享文章
     */
    SHARE_POST("分享文章"),
    
    // ==================== 学习行为 ====================
    /**
     * 注册课程
     */
    ENROLL_COURSE("注册课程"),
    
    /**
     * 完成章节
     */
    COMPLETE_CHAPTER("完成章节"),
    
    /**
     * 开始学习
     */
    START_LEARNING("开始学习"),
    
    /**
     * 激活CDK
     */
    ACTIVATE_CDK("激活CDK"),

    // ==================== OAuth 登录/绑定 ====================
    /**
     * OAuth 获取授权地址
     */
    OAUTH_AUTHORIZE_URL("获取OAuth授权地址"),

    /**
     * OAuth 回调（登录）
     */
    OAUTH_CALLBACK("OAuth回调登录"),

    /**
     * OAuth 邮箱合并
     */
    OAUTH_EMAIL_MERGE("OAuth邮箱合并"),

    /**
     * OAuth 绑定
     */
    OAUTH_BIND("OAuth绑定"),

    /**
     * OAuth 解绑
     */
    OAUTH_UNBIND("OAuth解绑"),
    
    // ==================== 管理操作 ====================
    /**
     * 管理员登录
     */
    ADMIN_LOGIN("管理员登录"),
    
    /**
     * 管理员更新用户
     */
    ADMIN_UPDATE_USER("管理员更新用户"),
    
    /**
     * 管理员删除文章
     */
    ADMIN_DELETE_POST("管理员删除文章"),
    
    /**
     * 管理员更新课程
     */
    ADMIN_UPDATE_COURSE("管理员更新课程");
    
    private final String description;
    
    ActivityType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 获取活动类型的分类
     * 
     * @return 对应的活动分类
     */
    public ActivityCategory getCategory() {
        switch (this) {
            case LOGIN_SUCCESS:
            case LOGIN_FAILED:
            case REGISTER_SUCCESS:
            case REGISTER_FAILED:
            case LOGOUT:
            case CHANGE_PASSWORD:
            case RESET_PASSWORD:
                return ActivityCategory.AUTHENTICATION;
                
            case VIEW_POST:
            case VIEW_COURSE:
            case VIEW_USER_PROFILE:
            case SEARCH_CONTENT:
                return ActivityCategory.BROWSING;
                
            case CREATE_POST:
            case UPDATE_POST:
            case DELETE_POST:
            case CREATE_COURSE:
            case UPDATE_COURSE:
            case DELETE_COURSE:
                return ActivityCategory.CONTENT_CREATION;
                
            case LIKE_POST:
            case UNLIKE_POST:
            case COMMENT_POST:
            case DELETE_COMMENT:
            case FOLLOW_USER:
            case UNFOLLOW_USER:
            case SHARE_POST:
                return ActivityCategory.SOCIAL_INTERACTION;
                
            case ENROLL_COURSE:
            case COMPLETE_CHAPTER:
            case START_LEARNING:
            case ACTIVATE_CDK:
                return ActivityCategory.LEARNING;
            case OAUTH_AUTHORIZE_URL:
            case OAUTH_CALLBACK:
            case OAUTH_EMAIL_MERGE:
            case OAUTH_BIND:
            case OAUTH_UNBIND:
                return ActivityCategory.AUTHENTICATION;
                
            case ADMIN_LOGIN:
            case ADMIN_UPDATE_USER:
            case ADMIN_DELETE_POST:
            case ADMIN_UPDATE_COURSE:
                return ActivityCategory.ADMINISTRATION;
                
            default:
                return ActivityCategory.OTHER;
        }
    }
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 枚举代码
     * @return 对应的枚举值，如果不存在则抛出异常
     */
    public static ActivityType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        try {
            return ActivityType.valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的活动类型: " + code);
        }
    }
}
