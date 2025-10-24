package org.xhy.community.infrastructure.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatLiveSessionRegistry {
    private static final Logger log = LoggerFactory.getLogger(ChatLiveSessionRegistry.class);

    // roomId -> sessions
    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    // sessionId -> roomIds
    private final Map<String, Set<String>> sessionRooms = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public ChatLiveSessionRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void subscribe(String roomId, WebSocketSession session) {
        roomSessions.computeIfAbsent(roomId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
        sessionRooms.computeIfAbsent(session.getId(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(roomId);
        int size = roomSessions.getOrDefault(roomId, Collections.emptySet()).size();
        Object uid = session.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
        log.info("[WS] subscribed: room={}, session={}, userId={}, onlineInRoom={}", roomId, session.getId(), uid, size);
    }

    public void unsubscribe(String roomId, WebSocketSession session) {
        Set<WebSocketSession> set = roomSessions.get(roomId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) roomSessions.remove(roomId);
        }
        Set<String> rooms = sessionRooms.get(session.getId());
        if (rooms != null) {
            rooms.remove(roomId);
            if (rooms.isEmpty()) sessionRooms.remove(session.getId());
        }
        int size = roomSessions.getOrDefault(roomId, Collections.emptySet()).size();
        Object uid = session.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
        log.info("[WS] unsubscribed: room={}, session={}, userId={}, onlineInRoom={}", roomId, session.getId(), uid, size);
    }

    public void removeSession(WebSocketSession session) {
        Set<String> rooms = sessionRooms.remove(session.getId());
        if (rooms != null) {
            for (String room : rooms) {
                Set<WebSocketSession> set = roomSessions.get(room);
                if (set != null) {
                    set.remove(session);
                    if (set.isEmpty()) roomSessions.remove(room);
                }
            }
        }
        log.info("[WS] removed session: {}", session.getId());
    }

    public void broadcast(String roomId, Object frame) {
        Set<WebSocketSession> set = roomSessions.get(roomId);
        if (set == null || set.isEmpty()) return;
        int ok = 0, fail = 0;
        for (WebSocketSession s : set) {
            if (s.isOpen()) {
                try {
                    String json = (frame instanceof String) ? (String) frame : objectMapper.writeValueAsString(frame);
                    s.sendMessage(new TextMessage(json));
                    ok++;
                } catch (IOException e) {
                    fail++;
                    log.warn("[WS] send failed: session={}, room={}", s.getId(), roomId, e);
                }
            }
        }
        log.info("[WS] broadcast: room={}, targets={}, sent={}, failed={}", roomId, set.size(), ok, fail);
    }

    /** 返回某个 session 当前订阅的房间集合（拷贝）。 */
    public java.util.Set<String> getRoomsForSession(String sessionId) {
        Set<String> rooms = sessionRooms.get(sessionId);
        return rooms == null ? java.util.Collections.emptySet() : new java.util.HashSet<>(rooms);
    }

    /** 判断指定用户在房间内是否仍有至少一个在线会话。 */
    public boolean isUserOnlineInRoom(String roomId, String userId) {
        Set<WebSocketSession> set = roomSessions.get(roomId);
        if (set == null || set.isEmpty()) return false;
        for (WebSocketSession s : set) {
            Object uid = s.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
            if (uid != null && userId.equals(uid.toString())) return true;
        }
        return false;
    }

    /**
     * 获取房间在线的用户ID集合（基于当前进程会话）。
     */
    public java.util.Set<String> getOnlineUserIds(String roomId) {
        Set<WebSocketSession> set = roomSessions.get(roomId);
        if (set == null || set.isEmpty()) return java.util.Collections.emptySet();
        java.util.Set<String> uids = new java.util.HashSet<>();
        for (WebSocketSession s : set) {
            Object uid = s.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
            if (uid != null) uids.add(uid.toString());
        }
        return uids;
    }
}
