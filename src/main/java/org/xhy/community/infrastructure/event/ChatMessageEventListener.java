package org.xhy.community.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.xhy.community.domain.chat.event.ChatMessageCreatedEvent;
import org.xhy.community.infrastructure.ws.ChatLiveSessionRegistry;
import org.xhy.community.infrastructure.ws.model.ChatMessagePush;
import org.xhy.community.infrastructure.ws.model.WsFrame;
import org.xhy.community.infrastructure.ws.model.ChatMentionPush;
import org.xhy.community.application.user.dto.UserPublicProfileDTO;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.application.chat.service.ChatNotificationAppService;

/**
 * 聊天消息事件监听器（为 WebSocket/SSE 做准备）
 * 事务提交后触发，避免回滚时误推送
 */
@Component
public class ChatMessageEventListener {
    private static final Logger log = LoggerFactory.getLogger(ChatMessageEventListener.class);

    private final ChatLiveSessionRegistry registry;
    private final UserAppService userAppService;
    private final ChatNotificationAppService chatNotificationAppService;

    public ChatMessageEventListener(ChatLiveSessionRegistry registry, UserAppService userAppService,
                                    ChatNotificationAppService chatNotificationAppService) {
        this.registry = registry;
        this.userAppService = userAppService;
        this.chatNotificationAppService = chatNotificationAppService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatMessageCreated(ChatMessageCreatedEvent event) {
        log.info("[ChatEvent] after-commit message: id={}, roomId={}, senderId={}",
                event.getMessageId(), event.getRoomId(), event.getSenderId());

        ChatMessagePush payload = new ChatMessagePush(
                event.getMessageId(), event.getRoomId(), event.getSenderId(),
                event.getContent(), event.getQuotedMessageId(), event.getMentionedUserIds(), event.getOccurredAt());

        try {
            UserPublicProfileDTO profile = userAppService.getUserPublicProfile(event.getSenderId());
            if (profile != null) {
                payload.setSenderName(profile.getName());
                payload.setSenderAvatar(profile.getAvatar());
                payload.setSenderTags(profile.getTags());
            }
        } catch (Exception e) {
            log.warn("[ChatEvent] enrich sender profile failed: senderId={}", event.getSenderId(), e);
        }
        WsFrame<ChatMessagePush> frame = new WsFrame<>("message", payload);
        registry.broadcast(event.getRoomId(), frame);

        // 处理 @提及：房内在线用户推送 mention 事件；其余用户发站内消息
        if (event.getMentionedUserIds() != null && !event.getMentionedUserIds().isEmpty()) {
            java.util.Set<String> uids = new java.util.LinkedHashSet<>(event.getMentionedUserIds());
            for (String uid : uids) {
                if (uid == null || uid.isBlank()) continue;
                boolean onlineInRoom = registry.isUserOnlineInRoom(event.getRoomId(), uid);
                if (onlineInRoom) {
                    ChatMentionPush mention = new ChatMentionPush(
                            event.getRoomId(), event.getMessageId(), event.getSenderId(), uid,
                            event.getContent(), event.getOccurredAt());
                    // 复用发送者资料，便于前端展示
                    try {
                        UserPublicProfileDTO profile = userAppService.getUserPublicProfile(event.getSenderId());
                        if (profile != null) {
                            mention.setSenderName(profile.getName());
                            mention.setSenderAvatar(profile.getAvatar());
                            mention.setSenderTags(profile.getTags());
                        }
                    } catch (Exception ignored) {}
                    registry.sendToUserInRoom(event.getRoomId(), uid, new WsFrame<>("mention", mention));
                } else {
                    // 站内消息（仅提及的接收者）
                    chatNotificationAppService.sendChatMentionNotification(
                            uid, event.getRoomId(), event.getMessageId(), event.getSenderId(), event.getContent());
                }
            }
        }
    }
}
