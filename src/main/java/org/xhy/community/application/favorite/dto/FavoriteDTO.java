package org.xhy.community.application.favorite.dto;

import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;

import java.time.LocalDateTime;

/**
 * 收藏记录DTO
 */
public class FavoriteDTO {

    /** 收藏ID */
    private String id;

    /** 用户ID */
    private String userId;

    /** 目标ID */
    private String targetId;

    /** 目标类型 */
    private FavoriteTargetType targetType;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public FavoriteTargetType getTargetType() { return targetType; }
    public void setTargetType(FavoriteTargetType targetType) { this.targetType = targetType; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
