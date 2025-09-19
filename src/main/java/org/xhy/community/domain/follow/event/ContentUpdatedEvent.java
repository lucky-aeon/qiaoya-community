package org.xhy.community.domain.follow.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 内容更新事件 - 包含关注者列表
 * 当用户发布新内容时触发，通知其关注者
 */
public class ContentUpdatedEvent {
    
    private final String authorId;        // 作者ID
    private final String authorName;      // 作者姓名
    private final String authorEmail;     // 作者邮箱
    private final String contentId;       // 内容ID
    private final String contentTitle;    // 内容标题
    private final String contentType;     // 内容类型（文章/课程）
    private final LocalDateTime updateTime; // 更新时间
    
    // 包含关注者列表（由follow领域提供）
    private final List<FollowerInfo> followers;
    
    public ContentUpdatedEvent(String authorId, String authorName, String authorEmail,
                             String contentId, String contentTitle, String contentType,
                             List<FollowerInfo> followers) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.contentId = contentId;
        this.contentTitle = contentTitle;
        this.contentType = contentType;
        this.followers = followers;
        this.updateTime = LocalDateTime.now();
    }
    
    // Getters
    public String getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getAuthorEmail() { return authorEmail; }
    public String getContentId() { return contentId; }
    public String getContentTitle() { return contentTitle; }
    public String getContentType() { return contentType; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public List<FollowerInfo> getFollowers() { return followers; }
    
    /**
     * 关注者信息
     */
    public static class FollowerInfo {
        private final String followerId;      // 关注者ID
        private final String followerName;   // 关注者姓名
        private final String followerEmail;  // 关注者邮箱
        
        public FollowerInfo(String followerId, String followerName, String followerEmail) {
            this.followerId = followerId;
            this.followerName = followerName;
            this.followerEmail = followerEmail;
        }
        
        public String getFollowerId() { return followerId; }
        public String getFollowerName() { return followerName; }
        public String getFollowerEmail() { return followerEmail; }
    }
}