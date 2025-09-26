package org.xhy.community.application.user.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.dto.UserPublicProfileDTO;
import org.xhy.community.domain.user.entity.UserEntity;

public class UserAssembler {
    
    public static UserDTO toDTO(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    public static UserPublicProfileDTO toPublicProfileDTO(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        
        UserPublicProfileDTO dto = new UserPublicProfileDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 将更新资料请求转换为用户实体（仅包含需要更新的字段）
     */
    public static UserEntity fromUpdateProfileRequest(org.xhy.community.interfaces.user.request.UpdateProfileRequest request, String userId) {
        if (request == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(userId);
        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setAvatar(request.getAvatar());
        return entity;
    }
}
