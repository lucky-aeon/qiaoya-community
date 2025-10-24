package org.xhy.community.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.xhy.community.domain.chat.event.ChatRoomDeletedEvent;
import org.xhy.community.infrastructure.ws.ChatLiveSessionRegistry;
import org.xhy.community.infrastructure.ws.model.RoomClosedPush;
import org.xhy.community.infrastructure.ws.model.WsFrame;

/**
 * 房间相关事件监听器
 */
@Component
public class ChatRoomEventListener {
    private static final Logger log = LoggerFactory.getLogger(ChatRoomEventListener.class);

    private final ChatLiveSessionRegistry registry;

    public ChatRoomEventListener(ChatLiveSessionRegistry registry) {
        this.registry = registry;
    }

    /**
     * 房间删除后，向该房间所有在线订阅者广播 room_closed
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRoomDeleted(ChatRoomDeletedEvent event) {
        log.info("[ChatEvent] room deleted: roomId={}, operatorId={}", event.getRoomId(), event.getOperatorId());
        RoomClosedPush payload = new RoomClosedPush(
                event.getRoomId(),
                event.getOccurredAt().toString(),
                event.getOperatorId()
        );
        WsFrame<RoomClosedPush> frame = new WsFrame<>("room_closed", payload);
        registry.broadcast(event.getRoomId(), frame);
    }
}

