package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.post.dto.PublicPostDTO;
import org.xhy.community.domain.post.entity.PostEntity;

public class PublicPostAssembler {
    
    public static PublicPostDTO toDTO(PostEntity entity) {
        if (entity == null) {
            return null;
        }
        
        PublicPostDTO dto = new PublicPostDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}