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
 * 聊天消息事件监听器
 *
 * 职责：
 * - 监听领域事件 ChatMessageCreatedEvent（在消息持久化事务提交后触发）
 * - 将该消息广播到所属房间的在线会话（WS 帧 type=message）
 * - 对被 @ 提及的用户：
 *   - 若该用户在线于房间，发送 type=mention 的 WS 定向帧
 *   - 若该用户不在线于房间，发送站内消息通知（非 WS）
 *
 * 选择 AFTER_COMMIT 的原因：
 * - 确保数据库事务提交成功后再推送，避免回滚导致“已推送但数据不存在”的不一致
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

    /**
     * 事务提交后广播消息与处理 @ 提及
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatMessageCreated(ChatMessageCreatedEvent event) {
        log.info("[ChatEvent] after-commit message: id={}, roomId={}, senderId={}",
                event.getMessageId(), event.getRoomId(), event.getSenderId());

        // 1) 构造消息推送载荷（必要字段从事件带入）
        ChatMessagePush payload = new ChatMessagePush(
                event.getMessageId(), event.getRoomId(), event.getSenderId(),
                event.getContent(), event.getQuotedMessageId(), event.getMentionedUserIds(), event.getOccurredAt());

        // 2) 尝试附加发送者公开资料（用于前端展示昵称/头像/标签）
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
        // 3) 广播消息帧：type=message
        WsFrame<ChatMessagePush> frame = new WsFrame<>("message", payload);
        registry.broadcast(event.getRoomId(), frame);

        // 4) 处理 @提及：
        //   - 在线于房间：定向 WS 帧 type=mention
        //   - 不在线于房间：发送站内消息（避免遗漏）
        if (event.getMentionedUserIds() != null && !event.getMentionedUserIds().isEmpty()) {
            java.util.Set<String> uids = new java.util.LinkedHashSet<>(event.getMentionedUserIds());
            for (String uid : uids) {
                if (uid == null || uid.isBlank()) continue;
                boolean onlineInRoom = registry.isUserOnlineInRoom(event.getRoomId(), uid);
                if (onlineInRoom) {
                    // 在线：准备 mention 推送载荷
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
                    // 定向推送：仅向该用户在该房间的会话下发
                    registry.sendToUserInRoom(event.getRoomId(), uid, new WsFrame<>("mention", mention));
                } else {
                    // 不在线：发送站内消息通知（仅提及的接收者），由前台提醒用户
                    chatNotificationAppService.sendChatMentionNotification(
                            uid, event.getRoomId(), event.getMessageId(), event.getSenderId(), event.getContent());
                }
            }
        }
    }
}
