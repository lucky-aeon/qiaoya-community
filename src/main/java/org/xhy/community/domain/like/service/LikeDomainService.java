package org.xhy.community.domain.like.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.like.entity.LikeEntity;
import org.xhy.community.domain.like.repository.LikeRepository;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.LikeErrorCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 点赞领域服务
 * 处理点赞相关的业务逻辑
 */
@Service
public class LikeDomainService {

    private final LikeRepository likeRepository;
    private static final Logger log = LoggerFactory.getLogger(LikeDomainService.class);

    public LikeDomainService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    /**
     * 切换点赞状态（Toggle，物理写入/删除）
     * 未点赞 -> 点赞（插入）
     * 已点赞 -> 取消（物理删除）
     */
    public boolean toggleLike(String userId, String targetId, LikeTargetType targetType) {
        LambdaQueryWrapper<LikeEntity> queryWrapper = new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getUserId, userId)
                .eq(LikeEntity::getTargetId, targetId)
                .eq(LikeEntity::getTargetType, targetType)
                .last("LIMIT 1");

        LikeEntity existingLike = likeRepository.selectOne(queryWrapper);
        if (existingLike == null) {
            try {
                LikeEntity like = new LikeEntity(userId, targetId, targetType);
                likeRepository.insert(like);
                log.info("【点赞】成功：userId={}, targetType={}, targetId={}", userId, targetType, targetId);
                return true; // 点赞成功
            } catch (DataIntegrityViolationException e) {
                // 唯一约束冲突视为已经点赞
                log.warn("【点赞】并发冲突：已点赞，userId={}, targetType={}, targetId={}", userId, targetType, targetId);
                throw new BusinessException(LikeErrorCode.ALREADY_LIKED);
            }
        } else {
            // 已点赞 -> 取消（物理删除）
            likeRepository.delete(new LambdaQueryWrapper<LikeEntity>()
                    .eq(LikeEntity::getUserId, userId)
                    .eq(LikeEntity::getTargetId, targetId)
                    .eq(LikeEntity::getTargetType, targetType));
            log.info("【点赞】已取消：userId={}, targetType={}, targetId={}", userId, targetType, targetId);
            return false;
        }
    }

    /**
     * 检查用户是否已点赞
     */
    public boolean isLiked(String userId, String targetId, LikeTargetType targetType) {
        LambdaQueryWrapper<LikeEntity> queryWrapper = new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getUserId, userId)
                .eq(LikeEntity::getTargetId, targetId)
                .eq(LikeEntity::getTargetType, targetType);

        return likeRepository.exists(queryWrapper);
    }

    /**
     * 批量查询点赞状态
     * @param userId 用户ID
     * @param targets 目标列表 Map<targetId, targetType>
     * @return Map<targetKey, isLiked> targetKey格式: targetType:targetId
     */
    public Map<String, Boolean> batchCheckLikeStatus(String userId, Map<String, LikeTargetType> targets) {
        if (targets.isEmpty()) {
            return new HashMap<>();
        }

        // 构建查询条件
        LambdaQueryWrapper<LikeEntity> queryWrapper = new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getUserId, userId)
                .and(wrapper -> {
                    for (Map.Entry<String, LikeTargetType> entry : targets.entrySet()) {
                        wrapper.or(w -> w.eq(LikeEntity::getTargetId, entry.getKey())
                                .eq(LikeEntity::getTargetType, entry.getValue()));
                    }
                });

        List<LikeEntity> likes = likeRepository.selectList(queryWrapper);

        // 转换为Map
        Set<String> likedKeys = likes.stream()
                .map(like -> buildTargetKey(like.getTargetType(), like.getTargetId()))
                .collect(Collectors.toSet());

        // 构建结果Map
        Map<String, Boolean> result = new HashMap<>();
        for (Map.Entry<String, LikeTargetType> entry : targets.entrySet()) {
            String key = buildTargetKey(entry.getValue(), entry.getKey());
            result.put(key, likedKeys.contains(key));
        }

        return result;
    }

    /**
     * 统计点赞数量
     */
    public long countLikes(String targetId, LikeTargetType targetType) {
        LambdaQueryWrapper<LikeEntity> queryWrapper = new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getTargetId, targetId)
                .eq(LikeEntity::getTargetType, targetType);

        return likeRepository.selectCount(queryWrapper);
    }

    /**
     * 批量统计点赞数量
     * @param targets Map<targetId, targetType>
     * @return Map<targetKey, count> targetKey格式: targetType:targetId
     */
    public Map<String, Long> batchCountLikes(Map<String, LikeTargetType> targets) {
        if (targets.isEmpty()) {
            return new HashMap<>();
        }

        // 按目标集合查询
        LambdaQueryWrapper<LikeEntity> queryWrapper = new LambdaQueryWrapper<LikeEntity>()
                .and(wrapper -> {
                    for (Map.Entry<String, LikeTargetType> entry : targets.entrySet()) {
                        wrapper.or(w -> w.eq(LikeEntity::getTargetId, entry.getKey())
                                .eq(LikeEntity::getTargetType, entry.getValue()));
                    }
                });

        List<LikeEntity> likes = likeRepository.selectList(queryWrapper);

        // 按targetKey分组统计
        Map<String, Long> countMap = likes.stream()
                .collect(Collectors.groupingBy(
                        like -> buildTargetKey(like.getTargetType(), like.getTargetId()),
                        Collectors.counting()
                ));

        // 补全未点赞的目标（count = 0）
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, LikeTargetType> entry : targets.entrySet()) {
            String key = buildTargetKey(entry.getValue(), entry.getKey());
            result.put(key, countMap.getOrDefault(key, 0L));
        }

        return result;
    }

    /**
     * 构建目标唯一标识：targetType:targetId
     */
    private String buildTargetKey(LikeTargetType targetType, String targetId) {
        return targetType.name() + ":" + targetId;
    }
}
