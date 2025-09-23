package org.xhy.community.application.follow.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.follow.dto.FollowDTO;
import org.xhy.community.domain.follow.entity.FollowEntity;
import org.xhy.community.domain.follow.query.FollowQuery;
import org.xhy.community.interfaces.follow.request.FollowQueryRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关注信息转换器
 */
public class FollowAssembler {
    
    /**
     * 实体转DTO
     */
    public static FollowDTO toDTO(FollowEntity entity) {
        if (entity == null) {
            return null;
        }
        
        FollowDTO dto = new FollowDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    /**
     * 实体列表转DTO列表
     */
    public static List<FollowDTO> toDTOList(List<FollowEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(FollowAssembler::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 实体转DTO（并设置目标名称）
     */
    public static FollowDTO toDTOWithTargetName(FollowEntity entity, String targetName) {
        if (entity == null) {
            return null;
        }
        FollowDTO dto = toDTO(entity);
        dto.setTargetName(targetName);
        return dto;
    }
    
    /**
     * 批量转换并设置目标名称
     */
    public static List<FollowDTO> toDTOListWithTargetNames(List<FollowEntity> entities, Map<String, String> targetNameMap) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(entity -> toDTOWithTargetName(entity, targetNameMap != null ? targetNameMap.get(entity.getTargetId()) : null))
                .collect(Collectors.toList());
    }

    /**
     * from request -> domain query (我的关注)
     */
    public static FollowQuery fromRequest(FollowQueryRequest request, String followerId) {
        FollowQuery query = new FollowQuery(request.getPageNum(), request.getPageSize());
        query.setFollowerId(followerId);
        query.setTargetType(request.getTargetType());
        return query;
    }
}
