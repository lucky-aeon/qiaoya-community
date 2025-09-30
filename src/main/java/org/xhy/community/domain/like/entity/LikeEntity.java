package org.xhy.community.domain.like.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.infrastructure.converter.LikeTargetTypeConverter;

@TableName("likes")
public class LikeEntity extends BaseEntity {

    /** 点赞用户ID */
    private String userId;

    /** 目标对象ID */
    private String targetId;

    /** 目标类型：COURSE/POST/CHAPTER/COMMENT */
    @TableField(typeHandler = LikeTargetTypeConverter.class)
    private LikeTargetType targetType;

    public LikeEntity() {
    }

    public LikeEntity(String userId, String targetId, LikeTargetType targetType) {
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    // Getters and Setters
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
}