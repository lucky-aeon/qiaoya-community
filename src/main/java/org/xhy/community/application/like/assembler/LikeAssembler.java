package org.xhy.community.application.like.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.like.dto.LikeCountDTO;
import org.xhy.community.application.like.dto.LikeDTO;
import org.xhy.community.application.like.dto.LikeStatusDTO;
import org.xhy.community.domain.like.entity.LikeEntity;
import org.xhy.community.domain.like.valueobject.LikeTargetType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 点赞信息转换器
 */
public class LikeAssembler {

    /**
     * 实体转DTO
     */
    public static LikeDTO toDTO(LikeEntity entity) {
        if (entity == null) {
            return null;
        }

        LikeDTO dto = new LikeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 实体列表转DTO列表
     */
    public static List<LikeDTO> toDTOList(List<LikeEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(LikeAssembler::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 构建点赞状态DTO
     */
    public static LikeStatusDTO toLikeStatusDTO(String targetId, LikeTargetType targetType, Boolean isLiked) {
        return new LikeStatusDTO(targetId, targetType, isLiked);
    }

    /**
     * 批量构建点赞状态DTO列表
     */
    public static List<LikeStatusDTO> toLikeStatusDTOList(Map<String, Boolean> statusMap, Map<String, LikeTargetType> targets) {
        return targets.entrySet().stream()
                .map(entry -> {
                    String targetId = entry.getKey();
                    LikeTargetType targetType = entry.getValue();
                    String key = targetType.name() + ":" + targetId;
                    Boolean isLiked = statusMap.getOrDefault(key, false);
                    return new LikeStatusDTO(targetId, targetType, isLiked);
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建点赞统计DTO
     */
    public static LikeCountDTO toLikeCountDTO(String targetId, LikeTargetType targetType, Long count) {
        return new LikeCountDTO(targetId, targetType, count);
    }

    /**
     * 批量构建点赞统计DTO列表
     */
    public static List<LikeCountDTO> toLikeCountDTOList(Map<String, Long> countMap, Map<String, LikeTargetType> targets) {
        return targets.entrySet().stream()
                .map(entry -> {
                    String targetId = entry.getKey();
                    LikeTargetType targetType = entry.getValue();
                    String key = targetType.name() + ":" + targetId;
                    Long count = countMap.getOrDefault(key, 0L);
                    return new LikeCountDTO(targetId, targetType, count);
                })
                .collect(Collectors.toList());
    }
}