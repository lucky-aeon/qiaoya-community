package org.xhy.community.application.favorite.dto;

import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;

/**
 * 收藏状态DTO（包含是否收藏和收藏数量）
 */
public class FavoriteStatusDTO {

    /** 目标ID */
    private String targetId;

    /** 目标类型 */
    private FavoriteTargetType targetType;

    /** 是否已收藏 */
    private Boolean isFavorited;

    /** 收藏数量 */
    private Long favoritesCount;

    public FavoriteStatusDTO() {
    }

    public FavoriteStatusDTO(String targetId, FavoriteTargetType targetType, Boolean isFavorited, Long favoritesCount) {
        this.targetId = targetId;
        this.targetType = targetType;
        this.isFavorited = isFavorited;
        this.favoritesCount = favoritesCount;
    }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public FavoriteTargetType getTargetType() { return targetType; }
    public void setTargetType(FavoriteTargetType targetType) { this.targetType = targetType; }

    public Boolean getIsFavorited() { return isFavorited; }
    public void setIsFavorited(Boolean isFavorited) { this.isFavorited = isFavorited; }

    public Long getFavoritesCount() { return favoritesCount; }
    public void setFavoritesCount(Long favoritesCount) { this.favoritesCount = favoritesCount; }
}
