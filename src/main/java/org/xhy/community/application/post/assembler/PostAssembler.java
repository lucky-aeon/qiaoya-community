package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.xhy.community.application.post.dto.PostDTO;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.interfaces.post.request.CreatePostRequest;
import org.xhy.community.interfaces.post.request.UpdatePostRequest;

import java.util.ArrayList;
import java.util.List;

public class PostAssembler {
    
    public static PostDTO toDTO(PostEntity entity) {
        if (entity == null) {
            return null;
        }
        
        PostDTO dto = new PostDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    public static List<PostDTO> toDTOList(List<PostEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<PostDTO> dtoList = new ArrayList<>();
        for (PostEntity entity : entities) {
            dtoList.add(toDTO(entity));
        }
        return dtoList;
    }
    
    public static PostEntity fromCreateRequest(CreatePostRequest request, String authorId) {
        if (request == null) {
            return null;
        }
        
        PostEntity entity = new PostEntity(
            request.getTitle(),
            request.getContent(), 
            authorId,
            request.getCategoryId()
        );
        
        entity.setSummary(request.getSummary());
        entity.setCoverImage(request.getCoverImage());
        
        return entity;
    }
    
    public static void updateEntityFromRequest(PostEntity entity, UpdatePostRequest request) {
        if (entity == null || request == null) {
            return;
        }
        
        if (StringUtils.hasText(request.getTitle())) {
            entity.setTitle(request.getTitle().trim());
        }
        
        if (request.getContent() != null) {
            entity.setContent(request.getContent());
        }
        
        // 这些字段允许设置为null（清空）
        entity.setSummary(request.getSummary());
        entity.setCoverImage(request.getCoverImage());
        
        if (request.getCategoryId() != null) {
            entity.setCategoryId(request.getCategoryId());
        }
    }
}