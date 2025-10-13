package org.xhy.community.domain.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.post.query.CategoryQuery;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.infrastructure.exception.PostErrorCode;
import org.xhy.community.domain.post.repository.CategoryRepository;
import org.xhy.community.domain.post.valueobject.CategoryType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryDomainService {
    private static final Logger log = LoggerFactory.getLogger(CategoryDomainService.class);

    private final CategoryRepository categoryRepository;
    
    public CategoryDomainService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    public CategoryEntity createCategory(String name, CategoryType type) {
        if (isCategoryNameExists(name, type, null, null)) {
            org.slf4j.LoggerFactory.getLogger(CategoryDomainService.class)
                    .warn("【分类】创建失败：名称重复，name={}, type={}", name, type);
            throw new BusinessException(PostErrorCode.CATEGORY_NAME_EXISTS);
        }
        
        CategoryEntity category = new CategoryEntity(name, type);
        categoryRepository.insert(category);
        return category;
    }
    
    public CategoryEntity createSubCategory(String name, String parentId, CategoryType type) {
        CategoryEntity parent = getCategoryById(parentId);
        
        if (!parent.getType().equals(type)) {
            org.slf4j.LoggerFactory.getLogger(CategoryDomainService.class)
                    .warn("【分类】创建子类失败：类型不匹配，parentId={}, parentType={}, requestType={}",
                            parentId, parent.getType(), type);
            throw new BusinessException(PostErrorCode.CATEGORY_TYPE_MISMATCH);
        }
        
        if (isCategoryNameExists(name, type, parentId, null)) {
            org.slf4j.LoggerFactory.getLogger(CategoryDomainService.class)
                    .warn("【分类】创建子类失败：名称重复，name={}, parentId={}", name, parentId);
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
            org.slf4j.LoggerFactory.getLogger(CategoryDomainService.class)
                    .warn("【分类】未找到：categoryId={}", categoryId);
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }
        
        return category;
    }
    
    public void updateCategory(String categoryId, String name, String description, String icon) {
        CategoryEntity category = getCategoryById(categoryId);
        
        if (name != null && !name.equals(category.getName())) {
            if (isCategoryNameExists(name, category.getType(), category.getParentId(), categoryId)) {
                org.slf4j.LoggerFactory.getLogger(CategoryDomainService.class)
                        .warn("【分类】更新失败：名称重复，categoryId={}, name={}", categoryId, name);
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

    /**
     * 统一更新路径：使用实体进行更新
     * 仅允许更新名称、描述、图标，保持与原有更新范围一致
     */
    public CategoryEntity updateCategory(CategoryEntity updated) {
        if (updated == null || updated.getId() == null) {
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }

        CategoryEntity category = getCategoryById(updated.getId());

        // 名称变更需要做唯一性校验（在同父级、同类型下）
        if (updated.getName() != null && !updated.getName().equals(category.getName())) {
            if (isCategoryNameExists(updated.getName(), category.getType(), category.getParentId(), category.getId())) {
                throw new BusinessException(PostErrorCode.CATEGORY_NAME_EXISTS);
            }
            category.setName(updated.getName());
        }

        if (updated.getDescription() != null) {
            category.setDescription(updated.getDescription());
        }

        if (updated.getIcon() != null) {
            category.setIcon(updated.getIcon());
        }

        categoryRepository.updateById(category);
        log.info("【分类】实体更新：categoryId={}", updated.getId());
        return category;
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
            .eq(CategoryEntity::getType, type)
            .eq(parentId != null, CategoryEntity::getParentId, parentId)
            .isNull(parentId == null, CategoryEntity::getParentId)
            .ne(excludeCategoryId != null, CategoryEntity::getId, excludeCategoryId);
        
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
    
    public void deleteCategory(String categoryId) {
        categoryRepository.deleteById(categoryId);
    }
    
    /**
     * 批量获取分类名称映射
     * 
     * @param categoryIds 分类ID集合
     * @return 分类ID到名称的映射
     */
    public Map<String, String> getCategoryNameMapByIds(Collection<String> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        
        List<CategoryEntity> categories = categoryRepository.selectBatchIds(categoryIds);
        return categories.stream()
                .collect(Collectors.toMap(
                    CategoryEntity::getId,
                    CategoryEntity::getName
                ));
    }
    
    public IPage<CategoryEntity> queryCategories(CategoryQuery query) {
        Page<CategoryEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<CategoryEntity>()
            .eq(query.getType() != null, CategoryEntity::getType, query.getType())
            .eq(StringUtils.hasText(query.getParentId()), CategoryEntity::getParentId, query.getParentId())
            .isNull(!StringUtils.hasText(query.getParentId()), CategoryEntity::getParentId)
            .eq(query.getIsActive() != null, CategoryEntity::getIsActive, query.getIsActive())
            .like(StringUtils.hasText(query.getName()), CategoryEntity::getName, query.getName())
            .orderByAsc(CategoryEntity::getSortOrder)
            .orderByDesc(CategoryEntity::getCreateTime);
        
        return categoryRepository.selectPage(page, queryWrapper);
    }
    
    public java.util.List<CategoryEntity> getAllActiveCategories(CategoryType type) {
        LambdaQueryWrapper<CategoryEntity> queryWrapper = new LambdaQueryWrapper<CategoryEntity>()
            .eq(CategoryEntity::getIsActive, true)
            .eq(type != null, CategoryEntity::getType, type)
            .orderByAsc(CategoryEntity::getLevel)
            .orderByAsc(CategoryEntity::getSortOrder)
            .orderByDesc(CategoryEntity::getCreateTime);
        
        return categoryRepository.selectList(queryWrapper);
    }
}
