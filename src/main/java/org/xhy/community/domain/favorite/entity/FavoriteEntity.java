package org.xhy.community.domain.favorite.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;
import org.xhy.community.infrastructure.converter.FavoriteTargetTypeConverter;

import java.time.LocalDateTime;

@TableName("favorites")
public class FavoriteEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 收藏用户ID */
    private String userId;

    /** 目标对象ID */
    private String targetId;

    /** 目标类型：POST/CHAPTER/COMMENT/INTERVIEW_QUESTION */
    @TableField(typeHandler = FavoriteTargetTypeConverter.class)
    private FavoriteTargetType targetType;

    public FavoriteEntity() {
    }

    public FavoriteEntity(String userId, String targetId, FavoriteTargetType targetType) {
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public FavoriteTargetType getTargetType() { return targetType; }
    public void setTargetType(FavoriteTargetType targetType) { this.targetType = targetType; }
}
