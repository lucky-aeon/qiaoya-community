package org.xhy.community.application.follow.dto;

import org.xhy.community.domain.follow.valueobject.FollowTargetType;

import java.util.Map;

/**
 * 关注统计DTO
 */
public class FollowStatisticsDTO {
    
    /** 总关注数 */
    private Long totalFollowers;
    
    /** 总关注的数量 */
    private Long totalFollowings;
    
    /** 按类型分组的关注数 */
    private Map<FollowTargetType, Long> followersByType;
    
    /** 按类型分组的关注的数量 */
    private Map<FollowTargetType, Long> followingsByType;
    
    /** 目标对象ID（用于查询特定对象的统计） */
    private String targetId;
    
    /** 目标对象类型 */
    private FollowTargetType targetType;
    
    /** 用户ID（用于查询特定用户的统计） */
    private String userId;
    
    public FollowStatisticsDTO() {}
    
    public FollowStatisticsDTO(Long totalFollowers, Long totalFollowings) {
        this.totalFollowers = totalFollowers;
        this.totalFollowings = totalFollowings;
    }
    
    // Getters and Setters
    public Long getTotalFollowers() { return totalFollowers; }
    public void setTotalFollowers(Long totalFollowers) { this.totalFollowers = totalFollowers; }
    
    public Long getTotalFollowings() { return totalFollowings; }
    public void setTotalFollowings(Long totalFollowings) { this.totalFollowings = totalFollowings; }
    
    public Map<FollowTargetType, Long> getFollowersByType() { return followersByType; }
    public void setFollowersByType(Map<FollowTargetType, Long> followersByType) { this.followersByType = followersByType; }
    
    public Map<FollowTargetType, Long> getFollowingsByType() { return followingsByType; }
    public void setFollowingsByType(Map<FollowTargetType, Long> followingsByType) { this.followingsByType = followingsByType; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public FollowTargetType getTargetType() { return targetType; }
    public void setTargetType(FollowTargetType targetType) { this.targetType = targetType; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}