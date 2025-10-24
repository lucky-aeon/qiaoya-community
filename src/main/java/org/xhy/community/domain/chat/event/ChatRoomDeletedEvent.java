package org.xhy.community.domain.chat.event;

import java.time.LocalDateTime;

/**
 * 房间删除（解散）事件，用于通知在线成员
 */
public class ChatRoomDeletedEvent {
    private final String roomId;
    private final String operatorId;
    private final LocalDateTime occurredAt;

    public ChatRoomDeletedEvent(String roomId, String operatorId) {
        this.roomId = roomId;
        this.operatorId = operatorId;
        this.occurredAt = LocalDateTime.now();
    }

    public String getRoomId() { return roomId; }
    public String getOperatorId() { return operatorId; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}

