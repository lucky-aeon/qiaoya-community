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

/**
 * 会话注册与房间内广播管理（进程内）
 *
 * 作用：
 * - 维护 roomId -> WebSocketSession 集合 的映射，支持订阅/退订
 * - 支持按房间广播、按房间+用户定向发送、查询在线用户等
 * - 仅维护当前应用实例（进程）的在线连接，未做跨实例共享
 *
 * 线程安全：
 * - 使用 ConcurrentHashMap + 基于它的 Set 实现以保证并发安全
 * - WebSocketSession 来自 Spring 管理，使用时需判断 isOpen()
 */
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

    /**
     * 订阅：将会话加入房间
     */
    public void subscribe(String roomId, WebSocketSession session) {
        // 建立双向关系：roomId -> session；sessionId -> roomId
        roomSessions.computeIfAbsent(roomId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
        sessionRooms.computeIfAbsent(session.getId(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(roomId);
        int size = roomSessions.getOrDefault(roomId, Collections.emptySet()).size();
        Object uid = session.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
        log.info("[WS] subscribed: room={}, session={}, userId={}, onlineInRoom={}", roomId, session.getId(), uid, size);
    }

    /**
     * 退订：将会话从房间移除
     */
    public void unsubscribe(String roomId, WebSocketSession session) {
        // 从房间集合中移除该会话
        Set<WebSocketSession> set = roomSessions.get(roomId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) roomSessions.remove(roomId);
        }
        // 从会话的订阅集合中移除该房间
        Set<String> rooms = sessionRooms.get(session.getId());
        if (rooms != null) {
            rooms.remove(roomId);
            if (rooms.isEmpty()) sessionRooms.remove(session.getId());
        }
        int size = roomSessions.getOrDefault(roomId, Collections.emptySet()).size();
        Object uid = session.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
        log.info("[WS] unsubscribed: room={}, session={}, userId={}, onlineInRoom={}", roomId, session.getId(), uid, size);
    }

    /**
     * 清理会话：连接关闭或异常时，移除该 session 关联的所有房间
     */
    public void removeSession(WebSocketSession session) {
        // 找出该会话订阅过的所有房间，并从这些房间的会话集合中删除它
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

    /**
     * 房间广播：向房间内所有在线会话发送一条消息
     *
     * 参数 frame 可为字符串（直接下发）或任意对象（将使用 ObjectMapper 序列化为 JSON）
     */
    public void broadcast(String roomId, Object frame) {
        Set<WebSocketSession> set = roomSessions.get(roomId);
        if (set == null || set.isEmpty()) return;
        int ok = 0, fail = 0;
        for (WebSocketSession s : set) {
            if (s.isOpen()) {
                try {
                    // 若 frame 是字符串则直接发送，否则序列化为 JSON 文本
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

    /**
     * 向房间内指定用户的所有在线会话发送消息。
     */
    public void sendToUserInRoom(String roomId, String userId, Object frame) {
        Set<WebSocketSession> set = roomSessions.get(roomId);
        if (set == null || set.isEmpty()) return;
        int ok = 0, fail = 0, total = 0;
        for (WebSocketSession s : set) {
            // 只向房间内 attribute userId 匹配的会话发送
            Object uid = s.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
            if (uid != null && userId.equals(uid.toString())) {
                total++;
                if (s.isOpen()) {
                    try {
                        String json = (frame instanceof String) ? (String) frame : objectMapper.writeValueAsString(frame);
                        s.sendMessage(new TextMessage(json));
                        ok++;
                    } catch (IOException e) {
                        fail++;
                        log.warn("[WS] sendToUser failed: session={}, room={}, userId={}", s.getId(), roomId, userId, e);
                    }
                }
            }
        }
        log.info("[WS] sendToUser: room={}, userId={}, targets={}, sent={}, failed={}", roomId, userId, total, ok, fail);
    }

    /** 返回某个 session 当前订阅的房间集合（拷贝）。 */
    /**
     * 查询某个会话当前订阅的房间集合（返回拷贝）
     */
    public java.util.Set<String> getRoomsForSession(String sessionId) {
        Set<String> rooms = sessionRooms.get(sessionId);
        return rooms == null ? java.util.Collections.emptySet() : new java.util.HashSet<>(rooms);
    }

    /** 判断指定用户在房间内是否仍有至少一个在线会话。 */
    /**
     * 判定用户是否在房间内在线（至少有一个会话在线）
     */
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
    /**
     * 获取房间内在线用户ID集合（当前实例内）
     */
    public java.util.Set<String> getOnlineUserIds(String roomId) {
        Set<WebSocketSession> set = roomSessions.get(roomId);
        if (set == null || set.isEmpty()) return java.util.Collections.emptySet();
        java.util.Set<String> uids = new java.util.HashSet<>();
        for (WebSocketSession s : set) {
            // 属性由握手拦截器注入
            Object uid = s.getAttributes().get(JwtHandshakeInterceptor.ATTR_USER_ID);
            if (uid != null) uids.add(uid.toString());
        }
        return uids;
    }
}
