package org.xhy.community.application.follow.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.follow.dto.FollowDTO;
import org.xhy.community.domain.follow.entity.FollowEntity;

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
     * 带扩展信息的实体转DTO
     */
    public static FollowDTO toDTOWithExtendedInfo(FollowEntity entity, String followerName, 
                                                 String followerAvatar, String targetName, 
                                                 String targetDescription, String authorName) {
        if (entity == null) {
            return null;
        }
        
        FollowDTO dto = toDTO(entity);
        dto.setFollowerName(followerName);
        dto.setFollowerAvatar(followerAvatar);
        dto.setTargetName(targetName);
        dto.setTargetDescription(targetDescription);
        dto.setAuthorName(authorName);
        
        return dto;
    }
    
    /**
     * 批量转换带扩展信息的DTO列表
     */
    public static List<FollowDTO> toDTOListWithExtendedInfo(List<FollowEntity> entities,
                                                           Map<String, String> followerNames,
                                                           Map<String, String> followerAvatars,
                                                           Map<String, String> targetNames,
                                                           Map<String, String> targetDescriptions,
                                                           Map<String, String> authorNames) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(entity -> toDTOWithExtendedInfo(
                    entity,
                    followerNames != null ? followerNames.get(entity.getFollowerId()) : null,
                    followerAvatars != null ? followerAvatars.get(entity.getFollowerId()) : null,
                    targetNames != null ? targetNames.get(entity.getTargetId()) : null,
                    targetDescriptions != null ? targetDescriptions.get(entity.getTargetId()) : null,
                    authorNames != null ? authorNames.get(entity.getTargetId()) : null
                ))
                .collect(Collectors.toList());
    }
}