package org.xhy.community.domain.like.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.infrastructure.converter.LikeTargetTypeConverter;

import java.time.LocalDateTime;

@TableName("likes")
public class LikeEntity {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

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

    public LikeTargetType getTargetType() { return targetType; }
    public void setTargetType(LikeTargetType targetType) { this.targetType = targetType; }
}
