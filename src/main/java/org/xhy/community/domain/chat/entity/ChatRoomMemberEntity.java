package org.xhy.community.domain.chat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.chat.valueobject.ChatRoomRole;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.infrastructure.converter.ChatRoomRoleConverter;

@TableName("chat_room_members")
public class ChatRoomMemberEntity extends BaseEntity {
    private String roomId;
    private String userId;

    @TableField(typeHandler = ChatRoomRoleConverter.class)
    private ChatRoomRole role;

    public ChatRoomMemberEntity() {}

    public ChatRoomMemberEntity(String roomId, String userId, ChatRoomRole role) {
        this.roomId = roomId;
        this.userId = userId;
        this.role = role;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public ChatRoomRole getRole() { return role; }
    public void setRole(ChatRoomRole role) { this.role = role; }
}

