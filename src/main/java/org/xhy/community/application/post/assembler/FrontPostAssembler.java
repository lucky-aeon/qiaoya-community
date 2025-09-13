package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.post.dto.FrontPostDTO;
import org.xhy.community.domain.post.entity.PostEntity;

import java.util.Map;

public class FrontPostAssembler {
    
    public static FrontPostDTO toDTO(PostEntity entity, String authorName, String categoryName) {
        if (entity == null) {
            return null;
        }
        
        FrontPostDTO dto = new FrontPostDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 设置关联信息
        dto.setAuthorName(authorName);
        dto.setCategoryName(categoryName);
        
        return dto;
    }
    
    public static FrontPostDTO toDTO(PostEntity entity, Map<String, String> authorNames, Map<String, String> categoryNames) {
        if (entity == null) {
            return null;
        }
        
        String authorName = authorNames.get(entity.getAuthorId());
        String categoryName = categoryNames.get(entity.getCategoryId());
        
        return toDTO(entity, authorName, categoryName);
    }
}