package org.xhy.community.application.like.helper;

import org.xhy.community.domain.like.service.LikeDomainService;
import org.xhy.community.domain.like.valueobject.LikeTargetType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 应用层点赞数填充工具
 * 统一封装批量与单体的点赞统计与回填，提升可读性与一致性
 */
public final class LikeCountHelper {

    private LikeCountHelper() {}

    /**
     * 为列表（单一目标类型）批量填充点赞数
     *
     * @param items 列表
     * @param idGetter 从元素获取目标ID
     * @param targetType 目标类型
     * @param likeCountSetter 设值器：为元素设置点赞数
     * @param likeDomainService 点赞领域服务
     * @param <T> 列表元素类型
     */
    public static <T> void fillLikeCount(List<T> items,
                                         Function<T, String> idGetter,
                                         LikeTargetType targetType,
                                         BiConsumer<T, Integer> likeCountSetter,
                                         LikeDomainService likeDomainService) {
        if (items == null || items.isEmpty()) {
            return;
        }

        Map<String, LikeTargetType> targets = new HashMap<>();
        for (T item : items) {
            String id = idGetter.apply(item);
            if (id != null) {
                targets.put(id, targetType);
            }
        }
        if (targets.isEmpty()) {
            return;
        }

        Map<String, Long> countMap = likeDomainService.batchCountLikes(targets);
        for (T item : items) {
            String id = idGetter.apply(item);
            if (id == null) {
                continue;
            }
            Long c = countMap.getOrDefault(targetType.name() + ":" + id, 0L);
            likeCountSetter.accept(item, Math.toIntExact(c));
        }
    }

    /**
     * 统计单个对象的点赞数并返回int
     */
    public static int getLikeCount(String targetId,
                                   LikeTargetType targetType,
                                   LikeDomainService likeDomainService) {
        long c = likeDomainService.countLikes(targetId, targetType);
        return Math.toIntExact(c);
    }
}

