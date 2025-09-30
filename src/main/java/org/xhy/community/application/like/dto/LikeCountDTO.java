package org.xhy.community.application.like.dto;

import org.xhy.community.domain.like.valueobject.LikeTargetType;

/**
 * 点赞统计DTO
 * 用于返回单个或批量统计点赞数
 */
public class LikeCountDTO {

    private String targetId;
    private LikeTargetType targetType;
    private Long count;

    public LikeCountDTO() {
    }

    public LikeCountDTO(String targetId, LikeTargetType targetType, Long count) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.count = count;
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

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}