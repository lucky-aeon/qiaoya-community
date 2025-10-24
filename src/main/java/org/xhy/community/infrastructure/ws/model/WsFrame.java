package org.xhy.community.infrastructure.ws.model;

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

