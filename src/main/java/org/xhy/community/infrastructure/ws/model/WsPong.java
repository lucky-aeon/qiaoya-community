package org.xhy.community.infrastructure.ws.model;

public class WsPong {
    private String serverTime;

    public WsPong() {}

    public WsPong(String serverTime) { this.serverTime = serverTime; }

    public String getServerTime() { return serverTime; }
    public void setServerTime(String serverTime) { this.serverTime = serverTime; }
}

