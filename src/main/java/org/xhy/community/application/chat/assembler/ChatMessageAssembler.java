package org.xhy.community.application.chat.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.chat.dto.ChatMessageDTO;
import org.xhy.community.domain.chat.entity.ChatMessageEntity;
import org.xhy.community.interfaces.chat.request.SendMessageRequest;
import org.xhy.community.domain.chat.query.ChatMessageQuery;
import org.xhy.community.interfaces.chat.request.RoomMessageQueryRequest;

import java.util.List;

public class ChatMessageAssembler {

    public static ChatMessageDTO toDTO(ChatMessageEntity entity) {
        if (entity == null) return null;
        ChatMessageDTO dto = new ChatMessageDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public static ChatMessageEntity fromSendRequest(String roomId, String senderId, SendMessageRequest request) {
        ChatMessageEntity entity = new ChatMessageEntity(roomId, senderId, request.getContent());
        entity.setQuotedMessageId(request.getQuotedMessageId());
        entity.setMentionedUserIds(request.getMentionedUserIds());
        return entity;
    }

    public static ChatMessageQuery fromPageRequest(String roomId, RoomMessageQueryRequest request) {
        return new ChatMessageQuery(roomId, request.getPageNum(), request.getPageSize());
    }
}
