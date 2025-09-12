package org.xhy.community.application.user.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.user.dto.UserDTO;
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
}