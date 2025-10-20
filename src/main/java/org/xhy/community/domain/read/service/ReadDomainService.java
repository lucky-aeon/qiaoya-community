package org.xhy.community.domain.read.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.common.valueobject.ReadChannel;
import org.xhy.community.domain.read.entity.UserLastSeenEntity;
import org.xhy.community.domain.read.repository.UserLastSeenRepository;

import java.time.LocalDateTime;

/**
 * 领域服务：管理用户的频道 Last Seen 记录（列表级未读）。
 * - 幂等初始化与更新
 * - 防回拨（lastSeenAt 单调不减）
 */
@Service
public class ReadDomainService {
    private static final Logger log = LoggerFactory.getLogger(ReadDomainService.class);

    private final UserLastSeenRepository userLastSeenRepository;

    public ReadDomainService(UserLastSeenRepository userLastSeenRepository) {
        this.userLastSeenRepository = userLastSeenRepository;
    }

    /**
     * 获取指定用户和频道的 Last Seen，若不存在则以 now 初始化（首次访问策略）。
     */
    public UserLastSeenEntity getOrInit(String userId, ReadChannel channel) {
        UserLastSeenEntity record = userLastSeenRepository.selectOne(
                new LambdaQueryWrapper<UserLastSeenEntity>()
                        .eq(UserLastSeenEntity::getUserId, userId)
                        .eq(UserLastSeenEntity::getChannel, channel)
        );
        if (record != null) {
            return record;
        }

        // 不存在则初始化；并发下以唯一键兜底（Flyway 建议加唯一约束 user_id+channel）
        UserLastSeenEntity init = new UserLastSeenEntity(userId, channel, LocalDateTime.now());
        try {
            userLastSeenRepository.insert(init);
            return init;
        } catch (DataIntegrityViolationException e) {
            // 并发插入导致唯一约束冲突，退化为查询返回已存在记录
            log.debug("Concurrent init lastSeen conflict, fallback to select. userId={}, channel={}", userId, channel);
            return userLastSeenRepository.selectOne(
                    new LambdaQueryWrapper<UserLastSeenEntity>()
                            .eq(UserLastSeenEntity::getUserId, userId)
                            .eq(UserLastSeenEntity::getChannel, channel)
            );
        }
    }

    /**
     * 读取 Last Seen（可能为 null，表示未初始化）。
     */
    public LocalDateTime getLastSeenAt(String userId, ReadChannel channel) {
        UserLastSeenEntity record = userLastSeenRepository.selectOne(
                new LambdaQueryWrapper<UserLastSeenEntity>()
                        .eq(UserLastSeenEntity::getUserId, userId)
                        .eq(UserLastSeenEntity::getChannel, channel)
        );
        return record == null ? null : record.getLastSeenAt();
    }

    /**
     * 用 serverNow 幂等更新 Last Seen（进入列表后的清零语义）。
     * - 若记录不存在：以 serverNow 初始化
     * - 若存在且 serverNow >= lastSeenAt：更新为 serverNow
     * - 若存在且 serverNow < lastSeenAt：忽略（防回拨）
     */
    public void updateLastSeen(String userId, ReadChannel channel, LocalDateTime serverNow) {
        UserLastSeenEntity record = userLastSeenRepository.selectOne(
                new LambdaQueryWrapper<UserLastSeenEntity>()
                        .eq(UserLastSeenEntity::getUserId, userId)
                        .eq(UserLastSeenEntity::getChannel, channel)
        );
        if (record == null) {
            // 初始化为 serverNow
            UserLastSeenEntity init = new UserLastSeenEntity(userId, channel, serverNow);
            try {
                userLastSeenRepository.insert(init);
                return;
            } catch (DataIntegrityViolationException e) {
                // 并发插入冲突则降级为更新
                log.debug("Concurrent insert on updateLastSeen, fallback to update. userId={}, channel={}", userId, channel);
            }
        }

        // 防回拨更新：仅当现有 lastSeenAt 为空或 <= serverNow 才更新
        LambdaUpdateWrapper<UserLastSeenEntity> update = new LambdaUpdateWrapper<UserLastSeenEntity>()
                .eq(UserLastSeenEntity::getUserId, userId)
                .eq(UserLastSeenEntity::getChannel, channel)
                .and(wrapper -> wrapper.isNull(UserLastSeenEntity::getLastSeenAt)
                        .or().le(UserLastSeenEntity::getLastSeenAt, serverNow))
                .set(UserLastSeenEntity::getLastSeenAt, serverNow);
        userLastSeenRepository.update(null, update);
    }
}

