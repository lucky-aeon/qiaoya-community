package org.xhy.community.application.like.dto;

import org.xhy.community.domain.like.valueobject.LikeTargetType;

/**
 * 点赞状态DTO
 * 用于返回单个或批量查询点赞状态
 */
public class LikeStatusDTO {

    private String targetId;
    private LikeTargetType targetType;
    private Boolean isLiked;

    public LikeStatusDTO() {
    }

    public LikeStatusDTO(String targetId, LikeTargetType targetType, Boolean isLiked) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.isLiked = isLiked;
    }

    // Getters and Setters
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

    public Boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(Boolean isLiked) {
        this.isLiked = isLiked;
    }
}