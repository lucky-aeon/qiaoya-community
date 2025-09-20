package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.post.dto.FrontPostDTO;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.user.entity.UserEntity;

import java.util.Map;

public class FrontPostAssembler {
    
    public static FrontPostDTO toDTO(PostEntity entity, UserEntity author, String categoryName) {
        if (entity == null) {
            return null;
        }

        FrontPostDTO dto = new FrontPostDTO();
        BeanUtils.copyProperties(entity, dto);

        // 设置关联信息
        if (author != null) {
            dto.setAuthorName(author.getName());
            dto.setAuthorAvatar(author.getAvatar());
        }
        dto.setCategoryName(categoryName);

        return dto;
    }

    public static FrontPostDTO toDTO(PostEntity entity, Map<String, UserEntity> authorMap, Map<String, String> categoryNames) {
        if (entity == null) {
            return null;
        }

        UserEntity author = authorMap.get(entity.getAuthorId());
        String categoryName = categoryNames.get(entity.getCategoryId());

        return toDTO(entity, author, categoryName);
    }

    public static FrontPostDTO toDTO(PostEntity entity, String authorName, String authorAvatar, String categoryName) {
        if (entity == null) {
            return null;
        }

        FrontPostDTO dto = new FrontPostDTO();
        BeanUtils.copyProperties(entity, dto);

        // 设置关联信息
        dto.setAuthorName(authorName);
        dto.setAuthorAvatar(authorAvatar);
        dto.setCategoryName(categoryName);

        return dto;
    }

    public static FrontPostDTO toDTO(PostEntity entity, String authorName, String categoryName) {
        return toDTO(entity, authorName, null, categoryName);
    }

    public static FrontPostDTO toDTO(PostEntity entity, Map<String, String> authorNames, Map<String, String> authorAvatars, Map<String, String> categoryNames) {
        if (entity == null) {
            return null;
        }

        String authorName = authorNames.get(entity.getAuthorId());
        String authorAvatar = authorAvatars.get(entity.getAuthorId());
        String categoryName = categoryNames.get(entity.getCategoryId());

        return toDTO(entity, authorName, authorAvatar, categoryName);
    }
}