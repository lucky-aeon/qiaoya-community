package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.xhy.community.application.post.dto.PostDTO;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.domain.post.query.PostQuery;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.interfaces.post.request.CreatePostRequest;
import org.xhy.community.interfaces.post.request.UpdatePostRequest;
import org.xhy.community.interfaces.post.request.PostQueryRequest;
import org.xhy.community.interfaces.post.request.AppPostQueryRequest;

import java.util.ArrayList;
import java.util.List;

public class PostAssembler {
    
    public static PostQuery fromUserRequest(PostQueryRequest request, String userId) {
        PostQuery query = new PostQuery(request.getPageNum(), request.getPageSize());
        query.setAuthorId(userId);
        query.setStatus(request.getStatus());
        query.setAccessLevel(AccessLevel.USER);
        return query;
    }
    
    public static PostQuery fromAppRequest(AppPostQueryRequest request) {
        PostQuery query = new PostQuery(request.getPageNum(), request.getPageSize());
        query.setCategoryType(request.getCategoryType());
        query.setCategoryId(request.getCategoryId());
        return query;
    }
    
    public static PostQuery fromAppRequest(AppPostQueryRequest request, String authorId) {
        PostQuery query = new PostQuery(request.getPageNum(), request.getPageSize());
        query.setCategoryType(request.getCategoryType());
        query.setCategoryId(request.getCategoryId());
        query.setAuthorId(authorId);
        return query;
    }
    
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
        entity.setTags(request.getTags());
        
        return entity;
    }
    
    public static PostEntity updateEntityFromRequest(String id,UpdatePostRequest request) {
        PostEntity postEntity = new PostEntity();
        BeanUtils.copyProperties(request, postEntity);
        postEntity.setId(id);
        return postEntity;
    }
}
