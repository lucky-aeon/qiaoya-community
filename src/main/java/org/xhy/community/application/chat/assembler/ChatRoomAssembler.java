package org.xhy.community.application.chat.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.chat.dto.ChatRoomDTO;
import org.xhy.community.domain.chat.entity.ChatRoomEntity;
import org.xhy.community.domain.chat.query.ChatRoomQuery;
import org.xhy.community.interfaces.chat.request.CreateChatRoomRequest;
import org.xhy.community.interfaces.chat.request.ChatRoomQueryRequest;

public class ChatRoomAssembler {

    public static ChatRoomDTO toDTO(ChatRoomEntity entity) {
        if (entity == null) return null;
        ChatRoomDTO dto = new ChatRoomDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public static ChatRoomDTO toDTOWithJoined(ChatRoomEntity entity, boolean joined) {
        ChatRoomDTO dto = toDTO(entity);
        if (dto != null) {
            dto.setJoined(joined);
        }
        return dto;
    }

    public static ChatRoomEntity fromCreateRequest(CreateChatRoomRequest request, String creatorId,
                                                   java.util.List<String> planIds,
                                                   org.xhy.community.domain.chat.valueobject.ChatRoomAudience audience) {
        ChatRoomEntity entity = new ChatRoomEntity(request.getName(), planIds, creatorId);
        entity.setDescription(request.getDescription());
        entity.setAudience(audience);
        return entity;
    }


    public static ChatRoomQuery fromPageRequest(ChatRoomQueryRequest request) {
        ChatRoomQuery query = new ChatRoomQuery(request.getPageNum(), request.getPageSize());
        query.setNameLike(request.getNameLike());
        return query;
    }
}
