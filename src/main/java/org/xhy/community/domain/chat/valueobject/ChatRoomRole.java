package org.xhy.community.domain.chat.valueobject;

public enum ChatRoomRole {
    OWNER("房主"),
    MEMBER("成员");

    private final String description;

    ChatRoomRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ChatRoomRole fromCode(String code) {
        for (ChatRoomRole role : values()) {
            if (role.name().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown chat room role: " + code);
    }
}
