package org.xhy.community.domain.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.infrastructure.exception.PostErrorCode;
import org.xhy.community.domain.post.repository.PostRepository;
import org.xhy.community.domain.post.valueobject.PostStatus;

import java.time.LocalDateTime;

@Service
public class PostDomainService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CategoryDomainService categoryDomainService;
    
    public PostEntity createPost(String title, String content, String authorId, String categoryId) {
        CategoryEntity category = categoryDomainService.getCategoryById(categoryId);
        if (category == null) {
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }
        
        if (!category.getIsActive()) {
            throw new BusinessException(PostErrorCode.CATEGORY_DISABLED, "分类已禁用，无法发布文章");
        }
        
        PostEntity post = new PostEntity(title, content, authorId, categoryId);
        postRepository.insert(post);
        return post;
    }
    
    public PostEntity getPostById(String postId) {
        return postRepository.selectOne(
            new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getDeleted, false)
        );
    }
    
    public void updatePost(String postId, String authorId, String title, String content, String summary, String coverImage, String categoryId) {
        PostEntity post = getPostById(postId);
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (!post.getAuthorId().equals(authorId)) {
            throw new BusinessException(PostErrorCode.UNAUTHORIZED_EDIT);
        }
        
        if (categoryId != null && !categoryId.equals(post.getCategoryId())) {
            CategoryEntity category = categoryDomainService.getCategoryById(categoryId);
            if (category == null) {
                throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
            }
            if (!category.getIsActive()) {
                throw new BusinessException(PostErrorCode.CATEGORY_DISABLED, "分类已禁用");
            }
            post.setCategoryId(categoryId);
        }
        
        if (title != null && !title.trim().isEmpty()) {
            post.setTitle(title.trim());
        }
        
        if (content != null) {
            post.setContent(content);
        }
        
        post.setSummary(summary);
        post.setCoverImage(coverImage);
        
        postRepository.updateById(post);
    }
    
    public void publishPost(String postId, String authorId) {
        PostEntity post = getPostById(postId);
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (!post.getAuthorId().equals(authorId)) {
            throw new BusinessException(PostErrorCode.UNAUTHORIZED_PUBLISH);
        }
        
        if (post.getStatus() == PostStatus.PUBLISHED) {
            throw new BusinessException(PostErrorCode.POST_ALREADY_PUBLISHED);
        }
        
        CategoryEntity category = categoryDomainService.getCategoryById(post.getCategoryId());
        if (category == null || !category.getIsActive()) {
            throw new BusinessException(PostErrorCode.CATEGORY_DISABLED, "分类不存在或已禁用，无法发布文章");
        }
        
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishTime(LocalDateTime.now());
        postRepository.updateById(post);
    }
    
    public void unpublishPost(String postId, String authorId) {
        PostEntity post = getPostById(postId);
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (!post.getAuthorId().equals(authorId)) {
            throw new BusinessException(PostErrorCode.UNAUTHORIZED_EDIT, "只能下架自己的文章");
        }
        
        if (post.getStatus() == PostStatus.DRAFT) {
            throw new BusinessException(PostErrorCode.POST_ALREADY_DRAFT);
        }
        
        post.setStatus(PostStatus.DRAFT);
        post.setPublishTime(null);
        postRepository.updateById(post);
    }
    
    public void setTopPost(String postId, boolean isTop) {
        PostEntity post = getPostById(postId);
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new BusinessException(PostErrorCode.POST_NOT_PUBLISHED);
        }
        
        post.setIsTop(isTop);
        postRepository.updateById(post);
    }
    
    public void incrementViewCount(String postId) {
        PostEntity post = getPostById(postId);
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        post.setViewCount(post.getViewCount() + 1);
        postRepository.updateById(post);
    }
    
    public void incrementLikeCount(String postId) {
        PostEntity post = getPostById(postId);
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.updateById(post);
    }
    
    public void decrementLikeCount(String postId) {
        PostEntity post = getPostById(postId);
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.updateById(post);
        }
    }
    
    public void incrementCommentCount(String postId) {
        PostEntity post = getPostById(postId);
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.updateById(post);
    }
    
    public void decrementCommentCount(String postId) {
        PostEntity post = getPostById(postId);
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (post.getCommentCount() > 0) {
            post.setCommentCount(post.getCommentCount() - 1);
            postRepository.updateById(post);
        }
    }
}