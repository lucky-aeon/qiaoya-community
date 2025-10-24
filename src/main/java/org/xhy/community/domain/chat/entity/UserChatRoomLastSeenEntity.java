package org.xhy.community.domain.chat.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("user_chat_room_last_seen")
public class UserChatRoomLastSeenEntity extends BaseEntity {
    private String userId;
    private String roomId;
    private LocalDateTime lastSeenAt;

    public UserChatRoomLastSeenEntity() {}

    public UserChatRoomLastSeenEntity(String userId, String roomId, LocalDateTime lastSeenAt) {
        this.userId = userId;
        this.roomId = roomId;
        this.lastSeenAt = lastSeenAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
}

