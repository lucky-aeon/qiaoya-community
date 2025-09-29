package org.xhy.community.domain.expression.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.infrastructure.converter.BusinessTypeConverter;

@TableName("reactions")
public class ReactionEntity extends BaseEntity {

    /** 业务类型：POST/COURSE/CHAPTER 等 */
    @TableField(typeHandler = BusinessTypeConverter.class)
    private BusinessType businessType;

    /** 业务ID（文章ID、课程ID等） */
    private String businessId;

    /** 用户ID */
    private String userId;

    /** 表情代码（对应 expression_types.code） */
    private String reactionType;

    public ReactionEntity() {}

    public ReactionEntity(BusinessType businessType, String businessId, String userId, String reactionType) {
        this.businessType = businessType;
        this.businessId = businessId;
        this.userId = userId;
        this.reactionType = reactionType;
    }

    // Getters and Setters
    public BusinessType getBusinessType() { return businessType; }
    public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }
}

