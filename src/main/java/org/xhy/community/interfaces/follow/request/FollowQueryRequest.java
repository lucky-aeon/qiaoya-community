package org.xhy.community.interfaces.follow.request;

import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.interfaces.common.request.PageRequest;

/**
 * 关注查询请求
 */
public class FollowQueryRequest extends PageRequest {
    
    /** 关注目标类型 */
    private FollowTargetType targetType;
    
    /** 关注者用户ID（管理员查询时使用） */
    private String followerId;
    
    /** 被关注目标ID（查询特定目标的关注者时使用） */
    private String targetId;
    
    public FollowQueryRequest() {}
    
    public FollowTargetType getTargetType() { return targetType; }
    public void setTargetType(FollowTargetType targetType) { this.targetType = targetType; }
    
    public String getFollowerId() { return followerId; }
    public void setFollowerId(String followerId) { this.followerId = followerId; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
}