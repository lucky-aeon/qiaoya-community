package org.xhy.community.infrastructure.ws.model;

/**
 * 订阅/退订操作的 ACK 负载
 */
public class WsAck {
    private String roomId;
    private String action; // subscribe/unsubscribe

    public WsAck() {}

    public WsAck(String roomId, String action) {
        this.roomId = roomId;
        this.action = action;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
