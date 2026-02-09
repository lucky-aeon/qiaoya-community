package org.xhy.community.infrastructure.ws.model;

/**
 * WebSocket 统一消息帧
 *
 * 语义：
 *  - type：事件类型，如 message/mention/presence/room_closed/error/pong/subscribed/unsubscribed
 *  - payload：负载，随 type 不同而变化
 */
public class WsFrame<T> {
    private String type;
    private T payload;

    public WsFrame() {}

    public WsFrame(String type, T payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public T getPayload() { return payload; }
    public void setPayload(T payload) { this.payload = payload; }
}
