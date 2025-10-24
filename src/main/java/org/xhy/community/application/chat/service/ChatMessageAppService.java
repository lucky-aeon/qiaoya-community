package org.xhy.community.application.chat.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.chat.assembler.ChatMessageAssembler;
import org.xhy.community.application.chat.dto.ChatMessageDTO;
import org.xhy.community.domain.chat.entity.ChatMessageEntity;
import org.xhy.community.domain.chat.service.ChatMessageDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.tag.service.TagDomainService;
import org.xhy.community.domain.tag.entity.UserTagAssignmentEntity;
import org.xhy.community.domain.tag.entity.TagDefinitionEntity;
import org.xhy.community.interfaces.chat.request.RoomMessageQueryRequest;
import org.xhy.community.interfaces.chat.request.SendMessageRequest;

import java.util.List;

@Service
public class ChatMessageAppService {

    private final ChatMessageDomainService chatMessageDomainService;
    private final UserDomainService userDomainService;
    private final TagDomainService tagDomainService;

    public ChatMessageAppService(ChatMessageDomainService chatMessageDomainService,
                                 UserDomainService userDomainService,
                                 TagDomainService tagDomainService) {
        this.chatMessageDomainService = chatMessageDomainService;
        this.userDomainService = userDomainService;
        this.tagDomainService = tagDomainService;
    }

    /** 用户发送消息（App层入参为 request 对象） */
    @Transactional(rollbackFor = Exception.class)
    public ChatMessageDTO sendMessage(String roomId, SendMessageRequest request, String senderId) {
        ChatMessageEntity message = ChatMessageAssembler.fromSendRequest(roomId, senderId, request);
        ChatMessageEntity created = chatMessageDomainService.sendMessage(message);
        ChatMessageDTO dto = ChatMessageAssembler.toDTO(created);
        enrichSenderInfo(java.util.List.of(dto));
        return dto;
    }


    /** 分页拉取消息（App层入参为 request 对象） */
    public IPage<ChatMessageDTO> pageMessages(String roomId, RoomMessageQueryRequest request,
                                              String operatorId) {
        var query = ChatMessageAssembler.fromPageRequest(roomId, request);
        IPage<ChatMessageEntity> page = chatMessageDomainService.pageMessages(query, operatorId);
        IPage<ChatMessageDTO> dtoPage = page.convert(ChatMessageAssembler::toDTO);
        if (dtoPage.getRecords() != null && !dtoPage.getRecords().isEmpty()) {
            enrichSenderInfo(dtoPage.getRecords());
        }
        return dtoPage;
    }

    private void enrichSenderInfo(java.util.List<ChatMessageDTO> dtos) {
        java.util.Set<String> userIds = dtos.stream()
                .map(ChatMessageDTO::getSenderId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        if (userIds.isEmpty()) return;

        java.util.Map<String, UserEntity> userMap = userDomainService.getUserEntityMapByIds(userIds);

        // 聚合所有用户的标签ID，批量查询定义
        java.util.Map<String, java.util.List<String>> userTagIdsMap = new java.util.HashMap<>();
        java.util.Set<String> allTagIds = new java.util.HashSet<>();
        for (String uid : userIds) {
            java.util.List<UserTagAssignmentEntity> assigns = tagDomainService.listIssuedAssignmentsByUser(uid);
            java.util.List<String> tagIds = assigns == null ? java.util.Collections.emptyList()
                    : assigns.stream().map(UserTagAssignmentEntity::getTagId)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            userTagIdsMap.put(uid, tagIds);
            allTagIds.addAll(tagIds);
        }
        java.util.Map<String, TagDefinitionEntity> defMap = allTagIds.isEmpty()
                ? java.util.Collections.emptyMap()
                : tagDomainService.getTagDefinitionMapByIds(allTagIds);

        for (ChatMessageDTO dto : dtos) {
            UserEntity u = userMap.get(dto.getSenderId());
            if (u != null) {
                dto.setSenderName(u.getName());
                dto.setSenderAvatar(u.getAvatar());
            }
            java.util.List<String> tagIds = userTagIdsMap.getOrDefault(dto.getSenderId(), java.util.Collections.emptyList());
            java.util.List<String> tagNames = tagIds.stream()
                    .map(defMap::get)
                    .filter(java.util.Objects::nonNull)
                    .map(TagDefinitionEntity::getName)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            dto.setSenderTags(tagNames);
        }
    }
}
