package org.xhy.community.infrastructure.ws.model;

public class WsPresence {
    private String roomId;
    private String userId;
    private boolean online; // true=加入/在线，false=退出/离线

    public WsPresence() {}

    public WsPresence(String roomId, String userId, boolean online) {
        this.roomId = roomId;
        this.userId = userId;
        this.online = online;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }
}

