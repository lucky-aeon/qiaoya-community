package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.post.dto.FrontPostDetailDTO;
import org.xhy.community.domain.post.entity.PostEntity;

/**
 * 公开文章详情组装器
 * 用于将PostEntity转换为FrontPostDetailDTO
 */
public class FrontPostDetailAssembler {
    
    public static FrontPostDetailDTO toDTO(PostEntity entity, String authorName, String categoryName) {
        if (entity == null) {
            return null;
        }
        
        FrontPostDetailDTO dto = new FrontPostDetailDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 设置关联信息
        dto.setAuthorName(authorName);
        dto.setCategoryName(categoryName);
        
        return dto;
    }
}