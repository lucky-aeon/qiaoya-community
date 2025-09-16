package org.xhy.community.application.post.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.post.assembler.CategoryAssembler;
import org.xhy.community.application.post.dto.CategoryTreeDTO;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.domain.post.service.CategoryDomainService;
import org.xhy.community.domain.post.valueobject.CategoryType;

import java.util.List;

@Service
public class AppCategoryAppService {
    
    private final CategoryDomainService categoryDomainService;
    
    public AppCategoryAppService(CategoryDomainService categoryDomainService) {
        this.categoryDomainService = categoryDomainService;
    }
    
    public List<CategoryTreeDTO> getCategoryTree(CategoryType type) {
        List<CategoryEntity> categories = categoryDomainService.getAllActiveCategories(type);
        return CategoryAssembler.buildCategoryTree(categories);
    }
}