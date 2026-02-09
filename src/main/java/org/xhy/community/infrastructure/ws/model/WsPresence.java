package org.xhy.community.infrastructure.ws.model;

/**
 * 在线态变更推送
 * - online=true：某用户进入了房间（该用户在该房间至少有一个会话在线）
 * - online=false：该用户离开了房间（该用户在该房间无会话在线）
 */
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
