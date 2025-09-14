package org.xhy.community.application.post.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.post.assembler.CategoryAssembler;
import org.xhy.community.application.post.dto.CategoryDTO;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.domain.post.service.CategoryDomainService;
import org.xhy.community.interfaces.post.request.CreateCategoryRequest;
import org.xhy.community.interfaces.post.request.UpdateCategoryRequest;
import org.xhy.community.interfaces.post.request.CategoryQueryRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminCategoryAppService {
    
    private final CategoryDomainService categoryDomainService;
    
    public AdminCategoryAppService(CategoryDomainService categoryDomainService) {
        this.categoryDomainService = categoryDomainService;
    }
    
    public CategoryDTO createCategory(CreateCategoryRequest request) {
        CategoryEntity category = CategoryAssembler.fromCreateRequest(request);
        
        CategoryEntity createdCategory;
        if (request.getParentId() != null && !request.getParentId().trim().isEmpty()) {
            createdCategory = categoryDomainService.createSubCategory(
                category.getName(), 
                category.getParentId(), 
                category.getType()
            );
        } else {
            createdCategory = categoryDomainService.createCategory(
                category.getName(), 
                category.getType()
            );
        }
        
        // 更新其他字段
        if (category.getDescription() != null || category.getIcon() != null || category.getSortOrder() != null) {
            categoryDomainService.updateCategory(
                createdCategory.getId(), 
                category.getName(), 
                category.getDescription(), 
                category.getIcon()
            );
            if (category.getSortOrder() != null) {
                createdCategory = categoryDomainService.getCategoryById(createdCategory.getId());
                createdCategory.setSortOrder(category.getSortOrder());
                categoryDomainService.updateCategory(
                    createdCategory.getId(),
                    createdCategory.getName(),
                    createdCategory.getDescription(),
                    createdCategory.getIcon()
                );
            }
            createdCategory = categoryDomainService.getCategoryById(createdCategory.getId());
        }
        
        return CategoryAssembler.toDTO(createdCategory);
    }
    
    public CategoryDTO updateCategory(String categoryId, UpdateCategoryRequest request) {
        categoryDomainService.updateCategory(
            categoryId,
            request.getName(),
            request.getDescription(),
            request.getIcon()
        );
        
        CategoryEntity updatedCategory = categoryDomainService.getCategoryById(categoryId);
        return CategoryAssembler.toDTO(updatedCategory);
    }
    
    public void deleteCategory(String categoryId) {
        categoryDomainService.deleteCategory(categoryId);
    }
    
    public CategoryDTO getCategoryById(String categoryId) {
        CategoryEntity category = categoryDomainService.getCategoryById(categoryId);
        return CategoryAssembler.toDTO(category);
    }
    
    public IPage<CategoryDTO> getPagedCategories(CategoryQueryRequest request) {
        IPage<CategoryEntity> categoryPage = categoryDomainService.getPagedCategories(
            request.getPageNum(),
            request.getPageSize(),
            request.getType(),
            request.getParentId()
        );
        
        return categoryPage.convert(CategoryAssembler::toDTO);
    }
}