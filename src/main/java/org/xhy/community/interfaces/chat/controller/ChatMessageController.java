package org.xhy.community.interfaces.chat.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.chat.dto.ChatMessageDTO;
import org.xhy.community.application.chat.service.ChatMessageAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.chat.request.RoomMessageQueryRequest;
import org.xhy.community.interfaces.chat.request.SendMessageRequest;

@RestController
@RequestMapping("/api/app/chat-rooms")
public class ChatMessageController {

    private final ChatMessageAppService chatMessageAppService;

    public ChatMessageController(ChatMessageAppService chatMessageAppService) {
        this.chatMessageAppService = chatMessageAppService;
    }

    @PostMapping("/{roomId}/messages")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "CHAT_MESSAGE_SEND", name = "发送聊天室消息")})
    public ApiResponse<ChatMessageDTO> sendMessage(@PathVariable String roomId,
                                                   @Valid @RequestBody SendMessageRequest request) {
        String userId = UserContext.getCurrentUserId();
        ChatMessageDTO dto = chatMessageAppService.sendMessage(roomId, request, userId);
        return ApiResponse.success("发送成功", dto);
    }

    @GetMapping("/{roomId}/messages")
    public ApiResponse<IPage<ChatMessageDTO>> pageMessages(@PathVariable String roomId,
                                                           RoomMessageQueryRequest request) {
        String userId = UserContext.getCurrentUserId();
        IPage<ChatMessageDTO> page = chatMessageAppService.pageMessages(roomId, request, userId);
        return ApiResponse.success(page);
    }
}
