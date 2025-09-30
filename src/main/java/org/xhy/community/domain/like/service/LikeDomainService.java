package org.xhy.community.domain.like.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.dao.DataIntegrityViolationException;
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

    public LikeDomainService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }

    /**
     * 切换点赞状态（Toggle）
     * 未点赞 -> 点赞（插入）
     * 已点赞 -> 取消（软删除）
     */
    public boolean toggleLike(String userId, String targetId, LikeTargetType targetType) {
        // 1. 查询是否存在点赞记录（包含已软删除的）
        LambdaQueryWrapper<LikeEntity> queryWrapper = new LambdaQueryWrapper<LikeEntity>()
                .eq(LikeEntity::getUserId, userId)
                .eq(LikeEntity::getTargetId, targetId)
                .eq(LikeEntity::getTargetType, targetType)
                .last("LIMIT 1");

        LikeEntity existingLike = likeRepository.selectOne(queryWrapper, false); // 查询包含已删除的记录

        if (existingLike == null) {
            // 2. 不存在 -> 创建新点赞（并发兜底）
            try {
                LikeEntity like = new LikeEntity(userId, targetId, targetType);
                likeRepository.insert(like);
                return true; // 返回true表示点赞成功
            } catch (DataIntegrityViolationException e) {
                // 并发情况下可能出现唯一约束冲突
                throw new BusinessException(LikeErrorCode.ALREADY_LIKED);
            }
        } else {
            // 3. 已存在 -> 判断是否已删除
            if (existingLike.getDeletedAt() == null) {
                // 未删除 -> 软删除（取消点赞）
                likeRepository.deleteById(existingLike.getId());
                return false; // 返回false表示取消点赞
            } else {
                // 已删除 -> 恢复记录（重新点赞）
                existingLike.setDeletedAt(null);
                likeRepository.updateById(existingLike);
                return true; // 返回true表示重新点赞
            }
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
                .eq(LikeEntity::getUserId, userId);

        // 添加OR条件：(targetId = ? AND targetType = ?) OR (targetId = ? AND targetType = ?)
        queryWrapper.and(wrapper -> {
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

        // 分组查询：按targetType和targetId分组统计
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