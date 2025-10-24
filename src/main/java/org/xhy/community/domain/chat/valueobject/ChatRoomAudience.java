package org.xhy.community.domain.chat.valueobject;

/**
 * 房间受众范围（加入判定逻辑说明）
 * - PAID_ONLY: 仅付费订阅用户可加入
 * - FREE_ONLY: 免费房间，付费用户也可加入（等同“有任一有效订阅”）
 * - ALL_USERS: 所有有订阅的用户（免费或付费），保留项用于未来更宽松策略
 */
public enum ChatRoomAudience {
    PAID_ONLY("仅付费订阅用户"),
    FREE_ONLY("免费房间（付费可进入）"),
    ALL_USERS("所有用户（保留项）");

    private final String description;

    ChatRoomAudience(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ChatRoomAudience fromCode(String code) {
        for (ChatRoomAudience a : values()) {
            if (a.name().equals(code)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Unknown chat room audience: " + code);
    }
}
