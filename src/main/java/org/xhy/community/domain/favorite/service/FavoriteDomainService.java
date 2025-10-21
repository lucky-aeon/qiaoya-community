package org.xhy.community.domain.favorite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.favorite.entity.FavoriteEntity;
import org.xhy.community.domain.favorite.query.FavoriteQuery;
import org.xhy.community.domain.favorite.repository.FavoriteRepository;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.FavoriteErrorCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 收藏领域服务
 * 处理收藏相关的业务逻辑
 */
@Service
public class FavoriteDomainService {

    private final FavoriteRepository favoriteRepository;
    private static final Logger log = LoggerFactory.getLogger(FavoriteDomainService.class);

    public FavoriteDomainService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    /**
     * 切换收藏状态（Toggle，物理写入/删除）
     * 未收藏 -> 收藏（插入）
     * 已收藏 -> 取消（物理删除）
     *
     * @return true表示收藏成功，false表示取消收藏
     */
    public boolean toggleFavorite(String userId, String targetId, FavoriteTargetType targetType) {
        LambdaQueryWrapper<FavoriteEntity> queryWrapper = new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getUserId, userId)
                .eq(FavoriteEntity::getTargetId, targetId)
                .eq(FavoriteEntity::getTargetType, targetType)
                .last("LIMIT 1");

        FavoriteEntity existingFavorite = favoriteRepository.selectOne(queryWrapper);
        if (existingFavorite == null) {
            try {
                FavoriteEntity favorite = new FavoriteEntity(userId, targetId, targetType);
                favoriteRepository.insert(favorite);
                return true; // 收藏成功
            } catch (DataIntegrityViolationException e) {
                // 唯一约束冲突视为已经收藏
                log.warn("【收藏】并发冲突：已收藏，userId={}, targetType={}, targetId={}", userId, targetType, targetId);
                throw new BusinessException(FavoriteErrorCode.ALREADY_FAVORITED);
            }
        } else {
            // 已收藏 -> 取消（物理删除）
            favoriteRepository.delete(new LambdaQueryWrapper<FavoriteEntity>()
                    .eq(FavoriteEntity::getUserId, userId)
                    .eq(FavoriteEntity::getTargetId, targetId)
                    .eq(FavoriteEntity::getTargetType, targetType));
            return false; // 取消收藏
        }
    }

    /**
     * 检查用户是否已收藏
     */
    public boolean isFavorited(String userId, String targetId, FavoriteTargetType targetType) {
        LambdaQueryWrapper<FavoriteEntity> queryWrapper = new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getUserId, userId)
                .eq(FavoriteEntity::getTargetId, targetId)
                .eq(FavoriteEntity::getTargetType, targetType);

        return favoriteRepository.exists(queryWrapper);
    }

    /**
     * 批量查询收藏状态
     * @param userId 用户ID
     * @param targets 目标列表 Map<targetId, targetType>
     * @return Map<targetKey, isFavorited> targetKey格式: targetType:targetId
     */
    public Map<String, Boolean> batchCheckFavoriteStatus(String userId, Map<String, FavoriteTargetType> targets) {
        if (targets.isEmpty()) {
            return new HashMap<>();
        }

        // 构建查询条件
        LambdaQueryWrapper<FavoriteEntity> queryWrapper = new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getUserId, userId)
                .and(wrapper -> {
                    for (Map.Entry<String, FavoriteTargetType> entry : targets.entrySet()) {
                        wrapper.or(w -> w.eq(FavoriteEntity::getTargetId, entry.getKey())
                                .eq(FavoriteEntity::getTargetType, entry.getValue()));
                    }
                });

        List<FavoriteEntity> favorites = favoriteRepository.selectList(queryWrapper);

        // 转换为Map
        Set<String> favoritedKeys = favorites.stream()
                .map(favorite -> buildTargetKey(favorite.getTargetType(), favorite.getTargetId()))
                .collect(Collectors.toSet());

        // 构建结果Map
        Map<String, Boolean> result = new HashMap<>();
        for (Map.Entry<String, FavoriteTargetType> entry : targets.entrySet()) {
            String key = buildTargetKey(entry.getValue(), entry.getKey());
            result.put(key, favoritedKeys.contains(key));
        }

        return result;
    }

    /**
     * 统计收藏数量
     */
    public long countFavorites(String targetId, FavoriteTargetType targetType) {
        LambdaQueryWrapper<FavoriteEntity> queryWrapper = new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getTargetId, targetId)
                .eq(FavoriteEntity::getTargetType, targetType);

        return favoriteRepository.selectCount(queryWrapper);
    }

    /**
     * 批量统计收藏数量
     * @param targets Map<targetId, targetType>
     * @return Map<targetKey, count> targetKey格式: targetType:targetId
     */
    public Map<String, Long> batchCountFavorites(Map<String, FavoriteTargetType> targets) {
        if (targets.isEmpty()) {
            return new HashMap<>();
        }

        // 按目标集合查询
        LambdaQueryWrapper<FavoriteEntity> queryWrapper = new LambdaQueryWrapper<FavoriteEntity>()
                .and(wrapper -> {
                    for (Map.Entry<String, FavoriteTargetType> entry : targets.entrySet()) {
                        wrapper.or(w -> w.eq(FavoriteEntity::getTargetId, entry.getKey())
                                .eq(FavoriteEntity::getTargetType, entry.getValue()));
                    }
                });

        List<FavoriteEntity> favorites = favoriteRepository.selectList(queryWrapper);

        // 按targetKey分组统计
        Map<String, Long> countMap = favorites.stream()
                .collect(Collectors.groupingBy(
                        favorite -> buildTargetKey(favorite.getTargetType(), favorite.getTargetId()),
                        Collectors.counting()
                ));

        // 补全未收藏的目标（count = 0）
        Map<String, Long> result = new HashMap<>();
        for (Map.Entry<String, FavoriteTargetType> entry : targets.entrySet()) {
            String key = buildTargetKey(entry.getValue(), entry.getKey());
            result.put(key, countMap.getOrDefault(key, 0L));
        }

        return result;
    }

    /**
     * 分页查询我的收藏（使用Query对象）
     * @param query 查询对象
     * @return 分页结果
     */
    public IPage<FavoriteEntity> pageMyFavorites(FavoriteQuery query) {
        Page<FavoriteEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<FavoriteEntity> queryWrapper = new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getUserId, query.getUserId())
                .eq(query.getTargetType() != null, FavoriteEntity::getTargetType, query.getTargetType())
                .orderByDesc(FavoriteEntity::getCreateTime);

        return favoriteRepository.selectPage(page, queryWrapper);
    }

    /**
     * 分页查询我的收藏（兼容旧接口）
     * @deprecated 使用 {@link #pageMyFavorites(FavoriteQuery)} 代替
     */
    @Deprecated
    public IPage<FavoriteEntity> pageMyFavorites(String userId, FavoriteTargetType targetType, Integer pageNum, Integer pageSize) {
        Page<FavoriteEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<FavoriteEntity> queryWrapper = new LambdaQueryWrapper<FavoriteEntity>()
                .eq(FavoriteEntity::getUserId, userId)
                .eq(targetType != null, FavoriteEntity::getTargetType, targetType)
                .orderByDesc(FavoriteEntity::getCreateTime);

        return favoriteRepository.selectPage(page, queryWrapper);
    }

    /**
     * 构建目标唯一标识：targetType:targetId
     */
    private String buildTargetKey(FavoriteTargetType targetType, String targetId) {
        return targetType.name() + ":" + targetId;
    }
}
