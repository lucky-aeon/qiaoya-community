package org.xhy.community.interfaces.chat.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

    public class CreateChatRoomRequest {
        @NotBlank(message = "房间名称不能为空")
        @Size(max = 128, message = "房间名称不能超过128字符")
        private String name;

        @Size(max = 2000, message = "房间描述不能超过2000字符")
        private String description;

        // 可选：管理员可指定多个套餐（普通用户不可指定，由默认套餐决定）
        private java.util.List<String> subscriptionPlanIds;

        // 可选：仅管理员生效，指定房间受众：
        // - PAID_ONLY: 仅付费订阅用户
        // - FREE_ONLY: 免费房间，付费也可进入（等同“有任一有效订阅”）
        // - ALL_USERS: 所有有订阅的用户（保留项）
        private org.xhy.community.domain.chat.valueobject.ChatRoomAudience audience;

    public CreateChatRoomRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public java.util.List<String> getSubscriptionPlanIds() { return subscriptionPlanIds; }
    public void setSubscriptionPlanIds(java.util.List<String> subscriptionPlanIds) { this.subscriptionPlanIds = subscriptionPlanIds; }

    public org.xhy.community.domain.chat.valueobject.ChatRoomAudience getAudience() { return audience; }
    public void setAudience(org.xhy.community.domain.chat.valueobject.ChatRoomAudience audience) { this.audience = audience; }
}
