package org.xhy.community.domain.chat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import org.apache.ibatis.type.JdbcType;
import org.xhy.community.infrastructure.converter.UniversalListConverter;
import org.xhy.community.infrastructure.converter.ChatRoomAudienceConverter;
import org.xhy.community.domain.chat.valueobject.ChatRoomAudience;

@TableName("chat_rooms")
public class ChatRoomEntity extends BaseEntity {
    private String name;
    private String description;
    @TableField(typeHandler = UniversalListConverter.class, jdbcType = JdbcType.OTHER, value = "subscription_plan_ids")
    private java.util.List<String> subscriptionPlanIds;
    private String creatorId;
    @TableField(typeHandler = ChatRoomAudienceConverter.class)
    private ChatRoomAudience audience;

    public ChatRoomEntity() {}

    public ChatRoomEntity(String name, java.util.List<String> subscriptionPlanIds, String creatorId) {
        this.name = name;
        this.subscriptionPlanIds = subscriptionPlanIds;
        this.creatorId = creatorId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public java.util.List<String> getSubscriptionPlanIds() { return subscriptionPlanIds; }
    public void setSubscriptionPlanIds(java.util.List<String> subscriptionPlanIds) { this.subscriptionPlanIds = subscriptionPlanIds; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public ChatRoomAudience getAudience() { return audience; }
    public void setAudience(ChatRoomAudience audience) { this.audience = audience; }
}
