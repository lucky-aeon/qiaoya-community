package org.xhy.community.application.user.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.user.dto.UserActivityLogDTO;
import org.xhy.community.domain.log.entity.UserActivityLogEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户活动日志转换器
 * 负责实体和DTO之间的转换
 */
public class UserActivityLogAssembler {
    
    /**
     * 将实体转换为DTO
     *
     * @param entity 用户活动日志实体
     * @return DTO对象
     */
    public static UserActivityLogDTO toDTO(UserActivityLogEntity entity) {
        if (entity == null) {
            return null;
        }
        
        UserActivityLogDTO dto = new UserActivityLogDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 设置活动类型描述
        if (entity.getActivityType() != null) {
            dto.setActivityTypeDesc(entity.getActivityType().getDescription());
        }
        
        return dto;
    }
    
    /**
     * 将实体列表转换为DTO列表（不包含用户昵称）
     *
     * @param entities 实体列表
     * @return DTO列表
     */
    public static List<UserActivityLogDTO> toDTOList(List<UserActivityLogEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        
        return entities.stream()
                .map(UserActivityLogAssembler::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 将实体列表转换为DTO列表（包含用户昵称）
     *
     * @param entities 实体列表
     * @param userNameMap 用户ID到昵称的映射
     * @return DTO列表
     */
    public static List<UserActivityLogDTO> toDTOListWithUserNames(List<UserActivityLogEntity> entities, Map<String, String> userNameMap) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        
        return entities.stream()
                .map(entity -> {
                    UserActivityLogDTO dto = toDTO(entity);
                    // 设置用户昵称
                    if (dto != null && entity.getUserId() != null) {
                        dto.setNickname(userNameMap.get(entity.getUserId()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }
}