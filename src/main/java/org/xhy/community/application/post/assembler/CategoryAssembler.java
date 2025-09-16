package org.xhy.community.application.post.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.post.dto.CategoryDTO;
import org.xhy.community.application.post.dto.CategoryTreeDTO;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.domain.post.query.CategoryQuery;
import org.xhy.community.interfaces.post.request.CreateCategoryRequest;
import org.xhy.community.interfaces.post.request.UpdateCategoryRequest;
import org.xhy.community.interfaces.post.request.CategoryQueryRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CategoryAssembler {
    
    public static CategoryQuery fromRequest(CategoryQueryRequest request) {
        if (request == null) {
            return null;
        }
        
        CategoryQuery query = new CategoryQuery(request.getPageNum(), request.getPageSize());
        query.setType(request.getType());
        query.setParentId(request.getParentId());
        return query;
    }
    
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
    
    public static CategoryTreeDTO toTreeDTO(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        
        CategoryTreeDTO dto = new CategoryTreeDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setChildren(new ArrayList<>());
        return dto;
    }
    
    public static List<CategoryTreeDTO> buildCategoryTree(List<CategoryEntity> categories) {
        if (categories == null || categories.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 转换为TreeDTO并按parentId分组
        Map<String, List<CategoryTreeDTO>> categoryMap = categories.stream()
            .map(CategoryAssembler::toTreeDTO)
            .collect(Collectors.groupingBy(dto -> dto.getParentId() == null ? "ROOT" : dto.getParentId()));
        
        // 构建树形结构
        List<CategoryTreeDTO> rootCategories = categoryMap.getOrDefault("ROOT", new ArrayList<>());
        buildChildren(rootCategories, categoryMap);
        
        return rootCategories;
    }
    
    private static void buildChildren(List<CategoryTreeDTO> parents, Map<String, List<CategoryTreeDTO>> categoryMap) {
        for (CategoryTreeDTO parent : parents) {
            List<CategoryTreeDTO> children = categoryMap.get(parent.getId());
            if (children != null && !children.isEmpty()) {
                parent.setChildren(children);
                buildChildren(children, categoryMap);
            }
        }
    }
}