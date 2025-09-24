package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.post.dto.FrontPostDetailDTO;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.user.entity.UserEntity;

/**
 * 公开文章详情组装器
 * 用于将PostEntity转换为FrontPostDetailDTO
 */
public class FrontPostDetailAssembler {
    
    public static FrontPostDetailDTO toDTO(PostEntity entity, UserEntity author, String categoryName) {
        if (entity == null) {
            return null;
        }

        FrontPostDetailDTO dto = new FrontPostDetailDTO();
        BeanUtils.copyProperties(entity, dto);

        // 设置关联信息
        if (author != null) {
            dto.setAuthorName(author.getName());
            dto.setAuthorAvatar(author.getAvatar());
            dto.setAuthorDescription(author.getDescription());
        }
        dto.setCategoryName(categoryName);

        return dto;
    }

    public static FrontPostDetailDTO toDTO(PostEntity entity, String authorName, String authorAvatar, String authorDescription, String categoryName) {
        if (entity == null) {
            return null;
        }

        FrontPostDetailDTO dto = new FrontPostDetailDTO();
        BeanUtils.copyProperties(entity, dto);

        // 设置关联信息
        dto.setAuthorName(authorName);
        dto.setAuthorAvatar(authorAvatar);
        dto.setAuthorDescription(authorDescription);
        dto.setCategoryName(categoryName);

        return dto;
    }

    public static FrontPostDetailDTO toDTO(PostEntity entity, String authorName, String categoryName) {
        return toDTO(entity, authorName, null, null, categoryName);
    }
}