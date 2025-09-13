package org.xhy.community.domain.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.infrastructure.exception.PostErrorCode;
import org.xhy.community.domain.post.repository.PostRepository;
import org.xhy.community.domain.post.valueobject.CategoryType;
import org.xhy.community.domain.post.valueobject.PostStatus;

import java.time.LocalDateTime;

@Service
public class PostDomainService {
    
    private final PostRepository postRepository;
    private final CategoryDomainService categoryDomainService;
    
    public PostDomainService(PostRepository postRepository, CategoryDomainService categoryDomainService) {
        this.postRepository = postRepository;
        this.categoryDomainService = categoryDomainService;
    }
    
    public PostEntity createPost(PostEntity post) {
        CategoryEntity category = categoryDomainService.getCategoryById(post.getCategoryId());
        if (category == null) {
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }
        
        if (!category.getIsActive()) {
            throw new BusinessException(PostErrorCode.CATEGORY_DISABLED);
        }
        
        postRepository.insert(post);
        return post;
    }
    
    public PostEntity getPostById(String postId) {
        PostEntity post = postRepository.selectOne(
            new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
        );
        
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        return post;
    }
    
    public PostEntity getUserPostById(String postId, String authorId) {
        PostEntity post = postRepository.selectOne(
            new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getAuthorId, authorId)
        );
        
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        return post;
    }
    
    public PostEntity updatePost(PostEntity post, String authorId) {
        // 验证分类是否可用
        CategoryEntity category = categoryDomainService.getCategoryById(post.getCategoryId());
        if (category == null) {
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }
        if (!category.getIsActive()) {
            throw new BusinessException(PostErrorCode.CATEGORY_DISABLED);
        }
        
        // 确保authorId设置到实体
        post.setAuthorId(authorId);
        
        // 使用条件更新，同时检查ID和作者ID
        LambdaUpdateWrapper<PostEntity> updateWrapper = new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, post.getId())
                .eq(PostEntity::getAuthorId, authorId);
        
        int updated = postRepository.update(post, updateWrapper);
        if (updated == 0) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        return post;
    }
    
    public PostEntity publishPost(String postId, String authorId) {
        // 查询文章（需要获取分类ID和当前状态）
        PostEntity post = postRepository.selectOne(
            new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getAuthorId, authorId)
        );
        
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (post.getStatus() == PostStatus.PUBLISHED) {
            throw new BusinessException(PostErrorCode.POST_ALREADY_PUBLISHED);
        }
        
        // 验证分类是否可用
        CategoryEntity category = categoryDomainService.getCategoryById(post.getCategoryId());
        if (category == null) {
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }
        if (!category.getIsActive()) {
            throw new BusinessException(PostErrorCode.CATEGORY_DISABLED);
        }
        
        // 使用条件更新
        LambdaUpdateWrapper<PostEntity> updateWrapper = new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getAuthorId, authorId)
                .set(PostEntity::getStatus, PostStatus.PUBLISHED)
                .set(PostEntity::getPublishTime, LocalDateTime.now());
        
        int updated = postRepository.update(null, updateWrapper);
        if (updated == 0) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        // 更新内存中的实体状态并返回
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishTime(LocalDateTime.now());
        return post;
    }
    
    public PostEntity unpublishPost(String postId, String authorId) {
        // 查询文章（需要获取当前状态）
        PostEntity post = postRepository.selectOne(
            new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getAuthorId, authorId)
        );
        
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (post.getStatus() == PostStatus.DRAFT) {
            throw new BusinessException(PostErrorCode.POST_ALREADY_DRAFT);
        }
        
        // 使用条件更新
        LambdaUpdateWrapper<PostEntity> updateWrapper = new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getAuthorId, authorId)
                .set(PostEntity::getStatus, PostStatus.DRAFT)
                .set(PostEntity::getPublishTime, null);
        
        int updated = postRepository.update(null, updateWrapper);
        if (updated == 0) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        // 更新内存中的实体状态并返回
        post.setStatus(PostStatus.DRAFT);
        post.setPublishTime(null);
        return post;
    }
    
    public void setTopPost(String postId, boolean isTop) {
        PostEntity post = getPostById(postId);
        
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(PostErrorCode.POST_NOT_PUBLISHED);
        }
        
        post.setIsTop(isTop);
        postRepository.updateById(post);
    }
    
    public void incrementViewCount(String postId) {
        PostEntity post = getPostById(postId);
        
        post.setViewCount(post.getViewCount() + 1);
        postRepository.updateById(post);
    }
    
    public void incrementCommentCount(String postId) {
        PostEntity post = getPostById(postId);
        
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.updateById(post);
    }
    
    public void decrementCommentCount(String postId) {
        PostEntity post = getPostById(postId);
        
        if (post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postRepository.updateById(post);
        }
    }
    
    public IPage<PostEntity> getUserPosts(String authorId, Integer pageNum, Integer pageSize, PostStatus status) {
        Page<PostEntity> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getAuthorId, authorId)
                .eq(status != null, PostEntity::getStatus, status)
                .orderByDesc(PostEntity::getCreateTime);
        
        return postRepository.selectPage(page, queryWrapper);
    }
    
    public void deletePost(String postId, String authorId) {
        LambdaQueryWrapper<PostEntity> deleteWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getAuthorId, authorId);
        
        int deleted = postRepository.delete(deleteWrapper);
        if (deleted == 0) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
    }
    
    public void updatePostFields(PostEntity post) {
        postRepository.updateById(post);
    }
    
    public IPage<PostEntity> queryPublicPosts(Integer page, Integer size, CategoryType categoryType) {
        Page<PostEntity> pageQuery = new Page<>(page, size);
        
        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getStatus, PostStatus.PUBLISHED)
                .orderByDesc(PostEntity::getCreateTime);
        
        // 如果指定了分类类型，需要关联查询分类表
        if (categoryType != null) {
            // 先查询符合条件的分类ID列表
            LambdaQueryWrapper<CategoryEntity> categoryWrapper = new LambdaQueryWrapper<CategoryEntity>()
                    .eq(CategoryEntity::getType, categoryType)
                    .eq(CategoryEntity::getIsActive, true)
                    .select(CategoryEntity::getId);
            
            java.util.List<CategoryEntity> categories = categoryDomainService.getCategories(categoryWrapper);
            if (categories.isEmpty()) {
                // 如果没有找到符合条件的分类，返回空结果
                return new Page<>(page, size, 0);
            }
            
            java.util.List<String> categoryIds = categories.stream()
                    .map(CategoryEntity::getId)
                    .toList();
            
            queryWrapper.in(PostEntity::getCategoryId, categoryIds);
        }
        
        return postRepository.selectPage(pageQuery, queryWrapper);
    }
}