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
    
    /**
     * 修改密码失败
     */
    CHANGE_PASSWORD_FAILED("修改密码失败"),
    
    /**
     * 用户主动下线设备
     */
    DEVICE_SESSION_TERMINATE("用户下线设备"),
    
    /**
     * 用户主动下线设备失败
     */
    DEVICE_SESSION_TERMINATE_FAILED("用户下线设备失败"),
    
    // ==================== 内容浏览 ====================
    /**
     * 查看文章
     */
    VIEW_POST("查看文章"),
    
    /**
     * 查看课程
     */
    VIEW_COURSE("查看课程"),

    /** 查看面试题 */
    VIEW_INTERVIEW_QUESTION("查看面试题"),
    
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
     * 发布文章
     */
    PUBLISH_POST("发布文章"),
    
    /**
     * 撤回文章
     */
    UNPUBLISH_POST("撤回文章"),
    
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

    /**
     * 下载资源
     */
    RESOURCE_DOWNLOAD("下载资源"),

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
    ADMIN_UPDATE_COURSE("管理员更新课程"),
    
    /**
     * 管理员强制下线
     */
    ADMIN_FORCE_LOGOUT("管理员强制下线"),
    
    /**
     * 管理员强制下线失败
     */
    ADMIN_FORCE_LOGOUT_FAILED("管理员强制下线失败"),
    
    /**
     * 移除用户黑名单
     */
    ADMIN_BLACKLIST_REMOVE("移除用户黑名单"),
    
    /**
     * 解除IP封禁
     */
    ADMIN_IP_UNBAN("解除IP封禁"),
    
    /**
     * 更新系统配置
     */
    ADMIN_UPDATE_CONFIG("更新系统配置"),
    
    /**
     * 更新系统配置失败
     */
    ADMIN_UPDATE_CONFIG_FAILED("更新系统配置失败"),
    
    /**
     * 管理员强制采纳评论
     */
    ADMIN_POST_FORCE_ACCEPT("管理员强制采纳评论"),
    
    /**
     * 管理员强制撤销采纳
     */
    ADMIN_POST_FORCE_REVOKE("管理员强制撤销采纳"),
    
    /**
     * 管理员创建分类
     */
    ADMIN_CATEGORY_CREATE("管理员创建分类"),
    
    /**
     * 管理员更新分类
     */
    ADMIN_CATEGORY_UPDATE("管理员更新分类"),
    
    /**
     * 管理员删除分类
     */
    ADMIN_CATEGORY_DELETE("管理员删除分类"),
    
    /**
     * 管理员创建章节
     */
    ADMIN_CHAPTER_CREATE("管理员创建章节"),
    
    /**
     * 管理员更新章节
     */
    ADMIN_CHAPTER_UPDATE("管理员更新章节"),
    
    /**
     * 管理员删除章节
     */
    ADMIN_CHAPTER_DELETE("管理员删除章节"),
    
    /**
     * 管理员排序章节
     */
    ADMIN_CHAPTER_REORDER("管理员排序章节"),
    
    /**
     * 管理员创建更新日志
     */
    ADMIN_UPDATE_LOG_CREATE("管理员创建更新日志"),
    
    /**
     * 管理员更新更新日志
     */
    ADMIN_UPDATE_LOG_UPDATE("管理员更新更新日志"),
    
    /**
     * 管理员删除更新日志
     */
    ADMIN_UPDATE_LOG_DELETE("管理员删除更新日志"),
    
    /**
     * 管理员切换更新日志状态
     */
    ADMIN_UPDATE_LOG_TOGGLE("管理员切换更新日志状态"),
    
    /**
     * 管理员创建CDK
     */
    ADMIN_CDK_CREATE("管理员创建CDK"),
    
    /**
     * 管理员删除CDK
     */
    ADMIN_CDK_DELETE("管理员删除CDK"),
    
    /**
     * 管理员解绑第三方账号
     */
    ADMIN_OAUTH_UNBIND("管理员解绑第三方账号"),
    
    /**
     * 管理员解绑第三方账号失败
     */
    ADMIN_OAUTH_UNBIND_FAILED("管理员解绑第三方账号失败"),

    // ==================== 表情管理 ====================
    /** 管理员创建表情类型 */
    ADMIN_EXPRESSION_CREATE("管理员创建表情类型"),
    /** 管理员更新表情类型 */
    ADMIN_EXPRESSION_UPDATE("管理员更新表情类型"),
    /** 管理员删除表情类型 */
    ADMIN_EXPRESSION_DELETE("管理员删除表情类型"),
    /** 管理员切换表情类型状态 */
    ADMIN_EXPRESSION_TOGGLE("管理员切换表情类型状态"),

    // ==================== 标签管理（管理员） ====================
    /** 管理员创建标签 */
    ADMIN_TAG_CREATE("管理员创建标签"),
    /** 管理员更新标签 */
    ADMIN_TAG_UPDATE("管理员更新标签"),
    /** 管理员添加标签范围 */
    ADMIN_TAG_ADD_SCOPE("管理员添加标签范围"),
    /** 管理员移除标签范围 */
    ADMIN_TAG_REMOVE_SCOPE("管理员移除标签范围"),
    /** 管理员手动发放标签 */
    ADMIN_TAG_ASSIGN("管理员手动发放标签"),
    /** 管理员撤销用户标签 */
    ADMIN_TAG_REVOKE("管理员撤销用户标签"),

    // ==================== 面试题管理（管理员） ====================
    /** 管理员更新面试题 */
    ADMIN_INTERVIEW_QUESTION_UPDATE("管理员更新面试题"),
    /** 管理员发布面试题 */
    ADMIN_INTERVIEW_QUESTION_PUBLISH("管理员发布面试题"),
    /** 管理员归档面试题 */
    ADMIN_INTERVIEW_QUESTION_ARCHIVE("管理员归档面试题"),
    /** 管理员删除面试题 */
    ADMIN_INTERVIEW_QUESTION_DELETE("管理员删除面试题"),

    // ==================== OAuth2客户端管理（管理员） ====================
    /** 管理员创建OAuth2客户端 */
    ADMIN_OAUTH2_CLIENT_CREATE("管理员创建OAuth2客户端"),
    /** 管理员更新OAuth2客户端 */
    ADMIN_OAUTH2_CLIENT_UPDATE("管理员更新OAuth2客户端"),
    /** 管理员删除OAuth2客户端 */
    ADMIN_OAUTH2_CLIENT_DELETE("管理员删除OAuth2客户端"),
    /** 管理员重新生成OAuth2客户端密钥 */
    ADMIN_OAUTH2_CLIENT_REGENERATE_SECRET("管理员重新生成OAuth2客户端密钥"),
    /** 管理员激活OAuth2客户端 */
    ADMIN_OAUTH2_CLIENT_ACTIVATE("管理员激活OAuth2客户端"),
    /** 管理员暂停OAuth2客户端 */
    ADMIN_OAUTH2_CLIENT_SUSPEND("管理员暂停OAuth2客户端"),
    /** 管理员撤销OAuth2客户端 */
    ADMIN_OAUTH2_CLIENT_REVOKE("管理员撤销OAuth2客户端");
    
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
            case CHANGE_PASSWORD_FAILED:
            case DEVICE_SESSION_TERMINATE:
            case DEVICE_SESSION_TERMINATE_FAILED:
                return ActivityCategory.AUTHENTICATION;
                
            case VIEW_POST:
            case VIEW_COURSE:
            case VIEW_USER_PROFILE:
            case SEARCH_CONTENT:
            case VIEW_INTERVIEW_QUESTION:
            case RESOURCE_DOWNLOAD:
                return ActivityCategory.BROWSING;
                
            case CREATE_POST:
            case UPDATE_POST:
            case DELETE_POST:
            case PUBLISH_POST:
            case UNPUBLISH_POST:
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
            case ADMIN_FORCE_LOGOUT:
            case ADMIN_FORCE_LOGOUT_FAILED:
            case ADMIN_BLACKLIST_REMOVE:
            case ADMIN_IP_UNBAN:
            case ADMIN_UPDATE_CONFIG:
            case ADMIN_UPDATE_CONFIG_FAILED:
            case ADMIN_POST_FORCE_ACCEPT:
            case ADMIN_POST_FORCE_REVOKE:
            case ADMIN_CATEGORY_CREATE:
            case ADMIN_CATEGORY_UPDATE:
            case ADMIN_CATEGORY_DELETE:
            case ADMIN_CHAPTER_CREATE:
            case ADMIN_CHAPTER_UPDATE:
            case ADMIN_CHAPTER_DELETE:
            case ADMIN_CHAPTER_REORDER:
            case ADMIN_UPDATE_LOG_CREATE:
            case ADMIN_UPDATE_LOG_UPDATE:
            case ADMIN_UPDATE_LOG_DELETE:
            case ADMIN_UPDATE_LOG_TOGGLE:
            case ADMIN_CDK_CREATE:
            case ADMIN_CDK_DELETE:
            case ADMIN_OAUTH_UNBIND:
            case ADMIN_OAUTH_UNBIND_FAILED:
            case ADMIN_EXPRESSION_CREATE:
            case ADMIN_EXPRESSION_UPDATE:
            case ADMIN_EXPRESSION_DELETE:
            case ADMIN_EXPRESSION_TOGGLE:
            case ADMIN_TAG_CREATE:
            case ADMIN_TAG_UPDATE:
            case ADMIN_TAG_ADD_SCOPE:
            case ADMIN_TAG_REMOVE_SCOPE:
            case ADMIN_TAG_ASSIGN:
            case ADMIN_TAG_REVOKE:
            case ADMIN_INTERVIEW_QUESTION_UPDATE:
            case ADMIN_INTERVIEW_QUESTION_PUBLISH:
            case ADMIN_INTERVIEW_QUESTION_ARCHIVE:
            case ADMIN_INTERVIEW_QUESTION_DELETE:
            case ADMIN_OAUTH2_CLIENT_CREATE:
            case ADMIN_OAUTH2_CLIENT_UPDATE:
            case ADMIN_OAUTH2_CLIENT_DELETE:
            case ADMIN_OAUTH2_CLIENT_REGENERATE_SECRET:
            case ADMIN_OAUTH2_CLIENT_ACTIVATE:
            case ADMIN_OAUTH2_CLIENT_SUSPEND:
            case ADMIN_OAUTH2_CLIENT_REVOKE:
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
