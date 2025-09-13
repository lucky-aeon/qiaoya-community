package org.xhy.community.domain.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.infrastructure.exception.PostErrorCode;
import org.xhy.community.domain.post.repository.CategoryRepository;
import org.xhy.community.domain.post.valueobject.CategoryType;

@Service
public class CategoryDomainService {
    
    private final CategoryRepository categoryRepository;
    
    public CategoryDomainService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    public CategoryEntity createCategory(String name, CategoryType type) {
        if (isCategoryNameExists(name, type, null, null)) {
            throw new BusinessException(PostErrorCode.CATEGORY_NAME_EXISTS);
        }
        
        CategoryEntity category = new CategoryEntity(name, type);
        categoryRepository.insert(category);
        return category;
    }
    
    public CategoryEntity createSubCategory(String name, String parentId, CategoryType type) {
        CategoryEntity parent = getCategoryById(parentId);
        
        if (!parent.getType().equals(type)) {
            throw new BusinessException(PostErrorCode.CATEGORY_TYPE_MISMATCH);
        }
        
        if (isCategoryNameExists(name, type, parentId, null)) {
            throw new BusinessException(PostErrorCode.CATEGORY_NAME_EXISTS);
        }
        
        CategoryEntity subCategory = new CategoryEntity(name, parentId, type, parent.getLevel() + 1);
        categoryRepository.insert(subCategory);
        return subCategory;
    }
    
    public CategoryEntity getCategoryById(String categoryId) {
        CategoryEntity category = categoryRepository.selectOne(
            new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getId, categoryId)
        );
        
        if (category == null) {
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }
        
        return category;
    }
    
    public void updateCategory(String categoryId, String name, String description, String icon) {
        CategoryEntity category = getCategoryById(categoryId);
        
        if (name != null && !name.equals(category.getName())) {
            if (isCategoryNameExists(name, category.getType(), category.getParentId(), categoryId)) {
                throw new BusinessException(PostErrorCode.CATEGORY_NAME_EXISTS);
            }
            category.setName(name);
        }
        
        if (description != null) {
            category.setDescription(description);
        }
        
        if (icon != null) {
            category.setIcon(icon);
        }
        
        categoryRepository.updateById(category);
    }
    
    public void activateCategory(String categoryId) {
        CategoryEntity category = getCategoryById(categoryId);
        
        category.setIsActive(true);
        categoryRepository.updateById(category);
    }
    
    public void deactivateCategory(String categoryId) {
        CategoryEntity category = getCategoryById(categoryId);
        
        category.setIsActive(false);
        categoryRepository.updateById(category);
    }
    
    public boolean isCategoryNameExists(String name, CategoryType type, String parentId, String excludeCategoryId) {
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<CategoryEntity>()
            .eq(CategoryEntity::getName, name)
            .eq(CategoryEntity::getType, type);

        if (parentId != null) {
            queryWrapper.eq(CategoryEntity::getParentId, parentId);
        } else {
            queryWrapper.isNull(CategoryEntity::getParentId);
        }
        
        if (excludeCategoryId != null) {
            queryWrapper.ne(CategoryEntity::getId, excludeCategoryId);
        }
        
        return categoryRepository.exists(queryWrapper);
    }
    
    public java.util.List<CategoryEntity> getCategories(LambdaQueryWrapper<CategoryEntity> queryWrapper) {
        return categoryRepository.selectList(queryWrapper);
    }
    
    public java.util.List<CategoryEntity> getCategoriesByIds(java.util.Collection<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return categoryRepository.selectBatchIds(categoryIds);
    }
}