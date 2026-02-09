package org.xhy.community.infrastructure.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.xhy.community.application.chat.service.ChatRoomAppService;
import org.xhy.community.infrastructure.ws.model.WsAck;
import org.xhy.community.infrastructure.ws.model.WsError;
import org.xhy.community.infrastructure.ws.model.WsFrame;
import org.xhy.community.infrastructure.ws.model.WsPong;
import org.xhy.community.infrastructure.ws.model.WsPresence;

import java.util.Map;

/**
 * WebSocket 文本消息处理器
 *
 * 协议（简单帧）：
 *  - 客户端 -> 服务端
 *    {"type":"SUBSCRIBE","roomId":"..."}   订阅房间
 *    {"type":"UNSUBSCRIBE","roomId":"..."}  退订房间
 *    {"type":"HEARTBEAT"}                      心跳
 *  - 服务端 -> 客户端（统一 WsFrame 包裹）
 *    type: "subscribed"|"unsubscribed"|"presence"|"message"|"mention"|"room_closed"|"error"|"pong"
 *
 * 权限：
 *  - 握手阶段完成 JWT 校验并注入 userId（见 JwtHandshakeInterceptor）
 *  - 订阅前通过 ChatRoomAppService.isMember 校验房间成员资格
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ChatLiveSessionRegistry registry;
    private final ChatRoomAppService chatRoomAppService;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ChatLiveSessionRegistry registry,
                                ChatRoomAppService chatRoomAppService,
                                ObjectMapper objectMapper) {
        this.registry = registry;
        this.chatRoomAppService = chatRoomAppService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 连接建立即可从握手阶段注入的 attributes 获取 userId
        String userId = getUserId(session);
        log.info("[WS] connected: user={}, session={}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            // 按约定：客户端发送的文本消息应为 JSON 对象，至少包含字段 `type`
            JsonNode root = objectMapper.readTree(message.getPayload());
            String type = root.path("type").asText("");
            switch (type) {
                case "SUBSCRIBE" -> handleSubscribe(session, root);    // 订阅房间
                case "UNSUBSCRIBE" -> handleUnsubscribe(session, root); // 退订房间
                case "HEARTBEAT" ->                                   // 心跳响应携带服务端时间
                        sendFrame(session, new WsFrame<>("pong", new WsPong(java.time.OffsetDateTime.now().toString())));
                default -> sendFrame(session, new WsFrame<>("error", new WsError("UNKNOWN_TYPE", "Unsupported message type: " + type)));
            }
        } catch (Exception e) {
            try {
                // 统一返回 error 帧，不抛出异常给容器，避免关闭连接
                sendFrame(session, new WsFrame<>("error", new WsError("BAD_REQUEST", e.getMessage())));
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserId(session);
        // 记录该会话之前订阅过的所有房间（在 remove 之前获取）
        java.util.Set<String> rooms = registry.getRoomsForSession(session.getId());
        // 从注册表清理当前会话与房间的关联
        registry.removeSession(session);
        // 对每个房间判断该用户是否仍有其他会话在线：若无，则广播 offline presence
        for (String roomId : rooms) {
            if (!registry.isUserOnlineInRoom(roomId, userId)) {
                registry.broadcast(roomId, new WsFrame<>("presence", new WsPresence(roomId, userId, false)));
            }
        }
        log.info("[WS] closed: session={}, status={}", session.getId(), status);
    }

    /**
     * 订阅房间：校验成员 -> 注册订阅 -> 回 ACK -> 如用户首次在线，广播 presence=true
     */
    private void handleSubscribe(WebSocketSession session, JsonNode root) throws Exception {
        String roomId = root.path("roomId").asText("");
        if (roomId.isEmpty()) {
            sendFrame(session, new WsFrame<>("error", new WsError("INVALID_ROOM", "roomId is required")));
            return;
        }
        String userId = getUserId(session);
        // 业务校验：仅允许房间成员订阅
        boolean isMember = chatRoomAppService.isMember(roomId, userId);
        if (!isMember) {
            sendFrame(session, new WsFrame<>("error", new WsError("UNAUTHORIZED_ROOM_ACCESS", "not a member")));
            return;
        }
        log.info("[WS] handle subscribe: roomId={}, userId={}, sessionId={}", roomId, userId, session.getId());
        // 订阅前判断该用户是否已在此房间在线（用于判定是否需要广播 presence=true）
        boolean wasOnline = registry.isUserOnlineInRoom(roomId, userId);
        registry.subscribe(roomId, session);
        sendFrame(session, new WsFrame<>("subscribed", new WsAck(roomId, "subscribe")));
        if (!wasOnline) {
            registry.broadcast(roomId, new WsFrame<>("presence", new WsPresence(roomId, userId, true)));
        }
    }

    /**
     * 退订房间：注销订阅 -> 回 ACK -> 如用户完全离线，广播 presence=false
     */
    private void handleUnsubscribe(WebSocketSession session, JsonNode root) throws Exception {
        String roomId = root.path("roomId").asText("");
        if (roomId.isEmpty()) {
            sendFrame(session, new WsFrame<>("error", new WsError("INVALID_ROOM", "roomId is required")));
            return;
        }
        log.info("[WS] handle unsubscribe: roomId={}, sessionId={}", roomId, session.getId());
        String userId = getUserId(session);
        // 从注册表移除订阅关系
        registry.unsubscribe(roomId, session);
        sendFrame(session, new WsFrame<>("unsubscribed", new WsAck(roomId, "unsubscribe")));
        // 若该用户在该房间已无任何会话在线，广播 presence=false
        if (!registry.isUserOnlineInRoom(roomId, userId)) {
            registry.broadcast(roomId, new WsFrame<>("presence", new WsPresence(roomId, userId, false)));
        }
    }

    /** 从握手拦截器注入的 attributes 中获取 userId */
    private String getUserId(WebSocketSession session) {
        Map<String, Object> attrs = session.getAttributes();
        Object val = attrs.get(JwtHandshakeInterceptor.ATTR_USER_ID);
        return val == null ? null : val.toString();
    }

    /** 序列化并下发统一帧 */
    private void sendFrame(WebSocketSession session, WsFrame<?> frame) throws Exception {
        String json = objectMapper.writeValueAsString(frame);
        session.sendMessage(new TextMessage(json));
    }
}
