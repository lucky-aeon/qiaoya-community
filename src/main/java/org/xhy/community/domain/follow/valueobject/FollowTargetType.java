package org.xhy.community.domain.follow.valueobject;

/**
 * 关注目标类型枚举
 * 定义可以被关注的对象类型
 */
public enum FollowTargetType {
    
    /** 关注用户 */
    USER("用户"),
    
    /** 关注课程 */
    COURSE("课程"),
    
    /** 关注章节 */
    CHAPTER("章节"),
    
    /** 关注文章 */
    POST("文章"),

    /** 关注题目 */
    INTERVIEW_QUESTION("题目");
    
    private final String description;
    
    FollowTargetType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
