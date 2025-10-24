package org.xhy.community.domain.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.chat.entity.UserChatRoomLastSeenEntity;
import org.xhy.community.domain.chat.repository.UserChatRoomLastSeenRepository;

import java.time.LocalDateTime;

@Service
public class ChatRoomReadDomainService {
    private final UserChatRoomLastSeenRepository repository;

    public ChatRoomReadDomainService(UserChatRoomLastSeenRepository repository) {
        this.repository = repository;
    }

    public UserChatRoomLastSeenEntity getOrInit(String userId, String roomId) {
        UserChatRoomLastSeenEntity rec = repository.selectOne(new LambdaQueryWrapper<UserChatRoomLastSeenEntity>()
                .eq(UserChatRoomLastSeenEntity::getUserId, userId)
                .eq(UserChatRoomLastSeenEntity::getRoomId, roomId));
        if (rec != null) return rec;
        UserChatRoomLastSeenEntity init = new UserChatRoomLastSeenEntity(userId, roomId, LocalDateTime.now());
        try {
            repository.insert(init);
            return init;
        } catch (DataIntegrityViolationException e) {
            return repository.selectOne(new LambdaQueryWrapper<UserChatRoomLastSeenEntity>()
                    .eq(UserChatRoomLastSeenEntity::getUserId, userId)
                    .eq(UserChatRoomLastSeenEntity::getRoomId, roomId));
        }
    }

    public LocalDateTime getLastSeenAt(String userId, String roomId) {
        UserChatRoomLastSeenEntity rec = repository.selectOne(new LambdaQueryWrapper<UserChatRoomLastSeenEntity>()
                .eq(UserChatRoomLastSeenEntity::getUserId, userId)
                .eq(UserChatRoomLastSeenEntity::getRoomId, roomId));
        return rec == null ? null : rec.getLastSeenAt();
    }

    public void updateLastSeen(String userId, String roomId, LocalDateTime serverNow) {
        UserChatRoomLastSeenEntity rec = repository.selectOne(new LambdaQueryWrapper<UserChatRoomLastSeenEntity>()
                .eq(UserChatRoomLastSeenEntity::getUserId, userId)
                .eq(UserChatRoomLastSeenEntity::getRoomId, roomId));
        if (rec == null) {
            UserChatRoomLastSeenEntity init = new UserChatRoomLastSeenEntity(userId, roomId, serverNow);
            try { repository.insert(init); return; } catch (DataIntegrityViolationException ignored) {}
        }
        repository.update(null, new LambdaUpdateWrapper<UserChatRoomLastSeenEntity>()
                .eq(UserChatRoomLastSeenEntity::getUserId, userId)
                .eq(UserChatRoomLastSeenEntity::getRoomId, roomId)
                .and(w -> w.isNull(UserChatRoomLastSeenEntity::getLastSeenAt).or()
                        .le(UserChatRoomLastSeenEntity::getLastSeenAt, serverNow))
                .set(UserChatRoomLastSeenEntity::getLastSeenAt, serverNow));
    }

    /** 批量读取房间的 lastSeenAt（单次查询）。 */
    public java.util.Map<String, LocalDateTime> getLastSeenForRooms(String userId, java.util.Set<String> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) return java.util.Collections.emptyMap();
        java.util.List<UserChatRoomLastSeenEntity> list = repository.selectList(
                new LambdaQueryWrapper<UserChatRoomLastSeenEntity>()
                        .eq(UserChatRoomLastSeenEntity::getUserId, userId)
                        .in(UserChatRoomLastSeenEntity::getRoomId, roomIds)
        );
        java.util.Map<String, LocalDateTime> map = new java.util.HashMap<>();
        for (UserChatRoomLastSeenEntity e : list) {
            map.put(e.getRoomId(), e.getLastSeenAt());
        }
        return map;
    }
}
