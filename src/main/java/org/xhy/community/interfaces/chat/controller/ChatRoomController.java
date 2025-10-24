package org.xhy.community.interfaces.chat.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.chat.dto.ChatRoomDTO;
import org.xhy.community.application.chat.dto.ChatRoomMemberDTO;
import org.xhy.community.application.chat.service.ChatRoomAppService;
import org.xhy.community.application.user.service.UserAppService;
import org.xhy.community.application.chat.service.ChatUnreadAppService;
import org.xhy.community.application.chat.dto.ChatUnreadInfoDTO;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.chat.request.CreateChatRoomRequest;
import org.xhy.community.interfaces.chat.request.ChatRoomQueryRequest;

@RestController
@RequestMapping("/api/app/chat-rooms")
public class ChatRoomController {

    private final ChatRoomAppService chatRoomAppService;
    private final UserAppService userAppService;
    private final ChatUnreadAppService chatUnreadAppService;

    public ChatRoomController(ChatRoomAppService chatRoomAppService, UserAppService userAppService,
                              ChatUnreadAppService chatUnreadAppService) {
        this.chatRoomAppService = chatRoomAppService;
        this.userAppService = userAppService;
        this.chatUnreadAppService = chatUnreadAppService;
    }

    @PostMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "CHAT_ROOM_CREATE", name = "创建聊天室")})
    public ApiResponse<ChatRoomDTO> createRoom(@Valid @RequestBody CreateChatRoomRequest request) {
        String userId = UserContext.getCurrentUserId();
        boolean isAdmin = userAppService.isAdmin(userId);
        ChatRoomDTO room = chatRoomAppService.createRoom(request, userId, isAdmin);
        return ApiResponse.success("创建成功", room);
    }

    @PostMapping("/{roomId}/join")
    public ApiResponse<Void> joinRoom(@PathVariable String roomId) {
        String userId = UserContext.getCurrentUserId();
        chatRoomAppService.joinRoom(roomId, userId);
        return ApiResponse.success("加入成功");
    }

    @PostMapping("/{roomId}/leave")
    public ApiResponse<Void> leaveRoom(@PathVariable String roomId) {
        String userId = UserContext.getCurrentUserId();
        chatRoomAppService.leaveRoom(roomId, userId);
        return ApiResponse.success("已退出房间");
    }

    /** 删除房间（仅房主） */
    @DeleteMapping("/{roomId}")
    public ApiResponse<Void> deleteRoom(@PathVariable String roomId) {
        String userId = UserContext.getCurrentUserId();
        chatRoomAppService.deleteRoom(roomId, userId);
        return ApiResponse.success("房间已删除");
    }

    @GetMapping
    public ApiResponse<IPage<ChatRoomDTO>> listRooms(ChatRoomQueryRequest request) {
        String userId = UserContext.getCurrentUserId();
        IPage<ChatRoomDTO> page = chatRoomAppService.pageAllRooms(request, userId);
        return ApiResponse.success(page);
    }

    @GetMapping("/{roomId}/members")
    public ApiResponse<java.util.List<ChatRoomMemberDTO>> listRoomMembers(@PathVariable String roomId) {
        String userId = UserContext.getCurrentUserId();
        java.util.List<ChatRoomMemberDTO> members = chatRoomAppService.listRoomMembers(roomId, userId);
        return ApiResponse.success(members);
    }

    /**
     * 查询房间未读消息数量（基于 Last Seen）。
     */
    @GetMapping("/{roomId}/unread-count")
    public ApiResponse<Long> getUnreadCount(@PathVariable String roomId) {
        String userId = UserContext.getCurrentUserId();
        long count = chatUnreadAppService.getUnreadCount(roomId, userId);
        return ApiResponse.success(count);
    }
    /**
     * 查询房间未读信息（数量 + 第一条未读锚点）。
     */
    @GetMapping("/{roomId}/unread-info")
    public ApiResponse<ChatUnreadInfoDTO> getUnreadInfo(@PathVariable String roomId) {
        String userId = UserContext.getCurrentUserId();
        ChatUnreadInfoDTO info = chatUnreadAppService.getUnreadInfo(roomId, userId);
        return ApiResponse.success(info);
    }

    /**
     * 进入房间后清零未读。
     * 可携带锚点：anchorId（消息ID优先）或 anchorTime（ISO-8601），用于把 lastSeen 推进到指定位置。
     * 不传则使用 serverNow。
     */
    @PutMapping("/{roomId}/visit")
    public ApiResponse<Void> visitRoom(@PathVariable String roomId,
                                       @RequestParam(value = "anchorId", required = false) String anchorId,
                                       @RequestParam(value = "anchorTime", required = false) String anchorTime) {
        String userId = UserContext.getCurrentUserId();
        if ((anchorId == null || anchorId.isBlank()) && (anchorTime == null || anchorTime.isBlank())) {
            chatUnreadAppService.visitRoom(roomId, userId);
        } else {
            chatUnreadAppService.visitRoom(roomId, userId, anchorId, anchorTime);
        }
        return ApiResponse.success();
    }
}
