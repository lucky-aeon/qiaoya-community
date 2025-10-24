package org.xhy.community.infrastructure.ws.model;

/**
 * WS 推送：房间被删除/解散
 */
public class RoomClosedPush {
    private String roomId;
    private String closedAt; // ISO-8601
    private String operatorId;

    public RoomClosedPush() {}

    public RoomClosedPush(String roomId, String closedAt, String operatorId) {
        this.roomId = roomId;
        this.closedAt = closedAt;
        this.operatorId = operatorId;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getClosedAt() { return closedAt; }
    public void setClosedAt(String closedAt) { this.closedAt = closedAt; }

    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
}

