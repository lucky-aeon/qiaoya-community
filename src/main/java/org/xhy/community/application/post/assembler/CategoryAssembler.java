package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.post.dto.CategoryDTO;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.interfaces.post.request.CreateCategoryRequest;
import org.xhy.community.interfaces.post.request.UpdateCategoryRequest;

public class CategoryAssembler {
    
    public static CategoryDTO toDTO(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        
        CategoryDTO dto = new CategoryDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    public static CategoryEntity fromCreateRequest(CreateCategoryRequest request) {
        if (request == null) {
            return null;
        }
        
        CategoryEntity entity = new CategoryEntity();
        BeanUtils.copyProperties(request, entity);
        
        return entity;
    }
    
    public static CategoryEntity fromUpdateRequest(UpdateCategoryRequest request, String categoryId) {
        if (request == null) {
            return null;
        }
        
        CategoryEntity entity = new CategoryEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(categoryId);
        
        return entity;
    }
}