package org.xhy.community.application.chat.service;

import org.springframework.stereotype.Service;
import org.xhy.community.domain.chat.service.ChatRoomReadDomainService;
import org.xhy.community.domain.chat.service.ChatMessageDomainService;

import java.time.LocalDateTime;

@Service
public class ChatUnreadAppService {
    private final ChatRoomReadDomainService chatRoomReadDomainService;
    private final ChatMessageDomainService chatMessageDomainService;

    public ChatUnreadAppService(ChatRoomReadDomainService chatRoomReadDomainService,
                                ChatMessageDomainService chatMessageDomainService) {
        this.chatRoomReadDomainService = chatRoomReadDomainService;
        this.chatMessageDomainService = chatMessageDomainService;
    }

    /** 查询房间未读数（基于 Last Seen）。 */
    public long getUnreadCount(String roomId, String userId) {
        LocalDateTime lastSeen = chatRoomReadDomainService.getLastSeenAt(userId, roomId);
        return chatMessageDomainService.countUnreadForUser(roomId, lastSeen, userId);
    }

    /** 进入房间后清零（将 lastSeen 设为 serverNow）。 */
    public void visitRoom(String roomId, String userId) {
        chatRoomReadDomainService.updateLastSeen(userId, roomId, LocalDateTime.now());
    }

    /** 查询未读数量 + 第一条未读锚点（用于入房定位）。 */
    public org.xhy.community.application.chat.dto.ChatUnreadInfoDTO getUnreadInfo(String roomId, String userId) {
        LocalDateTime lastSeen = chatRoomReadDomainService.getLastSeenAt(userId, roomId);
        long count = chatMessageDomainService.countUnreadForUser(roomId, lastSeen, userId);
        String firstId = null;
        String firstAt = null;
        if (count > 0) {
            var first = chatMessageDomainService.findFirstUnreadForUser(roomId, lastSeen, userId);
            if (first != null) {
                firstId = first.getId();
                firstAt = first.getCreateTime() != null ? first.getCreateTime().toString() : null;
            }
        }
        return new org.xhy.community.application.chat.dto.ChatUnreadInfoDTO(count, firstId, firstAt);
    }

    /**
     * 进入房间后清零（带锚点）。
     * 优先级：anchorId > anchorTime > now。
     */
    public void visitRoom(String roomId, String userId, String anchorId, String anchorTime) {
        LocalDateTime anchor = null;
        // 优先使用 anchorId 对应消息的时间
        if (anchorId != null && !anchorId.isBlank()) {
            var msg = chatMessageDomainService.getById(anchorId);
            if (msg != null && msg.getCreateTime() != null) {
                anchor = msg.getCreateTime();
            }
        }
        // 次选使用 anchorTime（支持 ISO-8601 或无偏移的本地时间）
        if (anchor == null && anchorTime != null && !anchorTime.isBlank()) {
            try {
                anchor = java.time.OffsetDateTime.parse(anchorTime).toLocalDateTime();
            } catch (Exception ignored) {
                try { anchor = java.time.LocalDateTime.parse(anchorTime); } catch (Exception ignored2) { }
            }
        }
        if (anchor == null) {
            anchor = LocalDateTime.now();
        }
        chatRoomReadDomainService.updateLastSeen(userId, roomId, anchor);
    }
}
