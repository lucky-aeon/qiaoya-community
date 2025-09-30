package org.xhy.community.application.like.dto;

import org.xhy.community.domain.like.valueobject.LikeTargetType;

import java.time.LocalDateTime;

/**
 * 点赞信息DTO
 */
public class LikeDTO {

    private String id;
    private String userId;
    private String targetId;
    private LikeTargetType targetType;
    private LocalDateTime createTime;

    public LikeDTO() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public LikeTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(LikeTargetType targetType) {
        this.targetType = targetType;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}