package org.xhy.community.application.favorite.assembler;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.xhy.community.application.favorite.dto.FavoriteDTO;
import org.xhy.community.application.favorite.dto.FavoriteListItemDTO;
import org.xhy.community.application.favorite.dto.FavoriteStatusDTO;
import org.xhy.community.domain.favorite.entity.FavoriteEntity;
import org.xhy.community.domain.favorite.query.FavoriteQuery;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;
import org.xhy.community.interfaces.favorite.request.MyFavoritesQueryRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏信息转换器
 */
public class FavoriteAssembler {

    /**
     * Request转Query
     */
    public static FavoriteQuery fromRequest(MyFavoritesQueryRequest request, String userId) {
        FavoriteQuery query = new FavoriteQuery(request.getPageNum(), request.getPageSize());
        query.setUserId(userId);
        query.setTargetType(request.getTargetType());
        return query;
    }

    /**
     * 实体转DTO
     */
    public static FavoriteDTO toDTO(FavoriteEntity entity) {
        if (entity == null) {
            return null;
        }

        FavoriteDTO dto = new FavoriteDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 实体列表转DTO列表
     */
    public static List<FavoriteDTO> toDTOList(List<FavoriteEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(FavoriteAssembler::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 构建收藏状态DTO
     */
    public static FavoriteStatusDTO toFavoriteStatusDTO(String targetId, FavoriteTargetType targetType,
                                                        Boolean isFavorited, Long favoritesCount) {
        return new FavoriteStatusDTO(targetId, targetType, isFavorited, favoritesCount);
    }

    /**
     * 批量构建收藏状态DTO列表
     */
    public static List<FavoriteStatusDTO> toFavoriteStatusDTOList(Map<String, Boolean> statusMap,
                                                                  Map<String, Long> countMap,
                                                                  Map<String, FavoriteTargetType> targets) {
        return targets.entrySet().stream()
                .map(entry -> {
                    String targetId = entry.getKey();
                    FavoriteTargetType targetType = entry.getValue();
                    String key = targetType.name() + ":" + targetId;
                    Boolean isFavorited = statusMap.getOrDefault(key, false);
                    Long count = countMap.getOrDefault(key, 0L);
                    return new FavoriteStatusDTO(targetId, targetType, isFavorited, count);
                })
                .collect(Collectors.toList());
    }

    /**
     * 实体转收藏列表项DTO（基础信息）
     */
    public static FavoriteListItemDTO toListItemDTO(FavoriteEntity entity) {
        if (entity == null) {
            return null;
        }

        FavoriteListItemDTO dto = new FavoriteListItemDTO();
        dto.setId(entity.getId());
        dto.setTargetId(entity.getTargetId());
        dto.setTargetType(entity.getTargetType());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }

    /**
     * 实体分页转收藏列表项DTO分页（基础信息）
     * 扩展信息需要在AppService中通过批量查询后填充
     */
    public static IPage<FavoriteListItemDTO> toListItemPage(IPage<FavoriteEntity> entityPage) {
        if (entityPage == null) {
            return null;
        }

        Page<FavoriteListItemDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<FavoriteListItemDTO> dtoList = entityPage.getRecords().stream()
                .map(FavoriteAssembler::toListItemDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }
}
