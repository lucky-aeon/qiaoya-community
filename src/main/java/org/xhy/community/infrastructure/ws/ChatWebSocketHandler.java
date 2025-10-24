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
        String userId = getUserId(session);
        log.info("[WS] connected: user={}, session={}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            String type = root.path("type").asText("");
            switch (type) {
                case "SUBSCRIBE" -> handleSubscribe(session, root);
                case "UNSUBSCRIBE" -> handleUnsubscribe(session, root);
                case "HEARTBEAT" -> sendFrame(session, new WsFrame<>("pong", new WsPong(java.time.OffsetDateTime.now().toString())));
                default -> sendFrame(session, new WsFrame<>("error", new WsError("UNKNOWN_TYPE", "Unsupported message type: " + type)));
            }
        } catch (Exception e) {
            try {
                sendFrame(session, new WsFrame<>("error", new WsError("BAD_REQUEST", e.getMessage())));
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserId(session);
        java.util.Set<String> rooms = registry.getRoomsForSession(session.getId());
        registry.removeSession(session);
        for (String roomId : rooms) {
            if (!registry.isUserOnlineInRoom(roomId, userId)) {
                registry.broadcast(roomId, new WsFrame<>("presence", new WsPresence(roomId, userId, false)));
            }
        }
        log.info("[WS] closed: session={}, status={}", session.getId(), status);
    }

    private void handleSubscribe(WebSocketSession session, JsonNode root) throws Exception {
        String roomId = root.path("roomId").asText("");
        if (roomId.isEmpty()) {
            sendFrame(session, new WsFrame<>("error", new WsError("INVALID_ROOM", "roomId is required")));
            return;
        }
        String userId = getUserId(session);
        boolean isMember = chatRoomAppService.isMember(roomId, userId);
        if (!isMember) {
            sendFrame(session, new WsFrame<>("error", new WsError("UNAUTHORIZED_ROOM_ACCESS", "not a member")));
            return;
        }
        log.info("[WS] handle subscribe: roomId={}, userId={}, sessionId={}", roomId, userId, session.getId());
        boolean wasOnline = registry.isUserOnlineInRoom(roomId, userId);
        registry.subscribe(roomId, session);
        sendFrame(session, new WsFrame<>("subscribed", new WsAck(roomId, "subscribe")));
        if (!wasOnline) {
            registry.broadcast(roomId, new WsFrame<>("presence", new WsPresence(roomId, userId, true)));
        }
    }

    private void handleUnsubscribe(WebSocketSession session, JsonNode root) throws Exception {
        String roomId = root.path("roomId").asText("");
        if (roomId.isEmpty()) {
            sendFrame(session, new WsFrame<>("error", new WsError("INVALID_ROOM", "roomId is required")));
            return;
        }
        log.info("[WS] handle unsubscribe: roomId={}, sessionId={}", roomId, session.getId());
        String userId = getUserId(session);
        registry.unsubscribe(roomId, session);
        sendFrame(session, new WsFrame<>("unsubscribed", new WsAck(roomId, "unsubscribe")));
        if (!registry.isUserOnlineInRoom(roomId, userId)) {
            registry.broadcast(roomId, new WsFrame<>("presence", new WsPresence(roomId, userId, false)));
        }
    }

    private String getUserId(WebSocketSession session) {
        Map<String, Object> attrs = session.getAttributes();
        Object val = attrs.get(JwtHandshakeInterceptor.ATTR_USER_ID);
        return val == null ? null : val.toString();
    }

    private void sendFrame(WebSocketSession session, WsFrame<?> frame) throws Exception {
        String json = objectMapper.writeValueAsString(frame);
        session.sendMessage(new TextMessage(json));
    }
}
