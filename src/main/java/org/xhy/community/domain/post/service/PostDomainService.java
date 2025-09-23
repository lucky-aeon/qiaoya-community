package org.xhy.community.domain.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.post.query.PostQuery;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.post.entity.CategoryEntity;
import org.xhy.community.domain.post.entity.PostEntity;
import org.xhy.community.infrastructure.exception.PostErrorCode;
import org.xhy.community.domain.post.repository.PostRepository;
import org.xhy.community.domain.post.repository.CategoryRepository;
import org.xhy.community.domain.post.valueobject.PostStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostDomainService {
    
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    
    public PostDomainService(PostRepository postRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
    }
    
    public PostEntity createPost(PostEntity post) {
        CategoryEntity category = categoryRepository.selectById(post.getCategoryId());
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
        CategoryEntity category = categoryRepository.selectById(post.getCategoryId());
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
        CategoryEntity category = categoryRepository.selectById(post.getCategoryId());
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
        // 原子自增，避免并发丢失
        LambdaUpdateWrapper<PostEntity> updateWrapper = new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .setSql("view_count = view_count + 1");
        int updated = postRepository.update(null, updateWrapper);
        if (updated == 0) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
    }
    
    public void incrementCommentCount(String postId) {
        // 原子自增
        LambdaUpdateWrapper<PostEntity> updateWrapper = new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .setSql("comment_count = comment_count + 1");
        int updated = postRepository.update(null, updateWrapper);
        if (updated == 0) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
    }
    
    public void decrementCommentCount(String postId) {
        // 原子自减（仅当 > 0）
        LambdaUpdateWrapper<PostEntity> updateWrapper = new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .gt(PostEntity::getCommentCount, 0)
                .setSql("comment_count = comment_count - 1");
        postRepository.update(null, updateWrapper);
    }
    
    /**
     * 批量获取文章标题映射
     * 
     * @param postIds 文章ID集合
     * @return 文章ID到标题的映射
     */
    public Map<String, String> getPostTitleMapByIds(Collection<String> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }
        
        List<PostEntity> posts = postRepository.selectBatchIds(postIds);
        return posts.stream()
                .collect(Collectors.toMap(
                    PostEntity::getId,
                    PostEntity::getTitle
                ));
    }
    
    public IPage<PostEntity> queryPosts(PostQuery query) {
        Page<PostEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(query.getAccessLevel() == AccessLevel.USER && StringUtils.hasText(query.getAuthorId()), PostEntity::getAuthorId, query.getAuthorId())
                .eq(query.getStatus() != null, PostEntity::getStatus, query.getStatus())
                .eq(StringUtils.hasText(query.getCategoryId()), PostEntity::getCategoryId, query.getCategoryId())
                .like(StringUtils.hasText(query.getTitle()), PostEntity::getTitle, query.getTitle())
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
    
    public IPage<PostEntity> queryAppPosts(org.xhy.community.domain.post.query.PostQuery query) {
        Page<PostEntity> pageQuery = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getStatus, PostStatus.PUBLISHED)
                .orderByDesc(PostEntity::getCreateTime);
        
        // 如果指定了分类类型，需要关联查询分类表
        if (query.getCategoryType() != null) {
            // 先查询符合条件的分类ID列表
            LambdaQueryWrapper<CategoryEntity> categoryWrapper = new LambdaQueryWrapper<CategoryEntity>()
                    .eq(CategoryEntity::getType, query.getCategoryType())
                    .eq(CategoryEntity::getIsActive, true)
                    .select(CategoryEntity::getId);
            
            java.util.List<CategoryEntity> categories = categoryRepository.selectList(categoryWrapper);
            if (categories.isEmpty()) {
                // 如果没有找到符合条件的分类，返回空结果
                return new Page<>(query.getPageNum(), query.getPageSize(), 0);
            }
            
            java.util.List<String> categoryIds = categories.stream()
                    .map(CategoryEntity::getId)
                    .toList();
            
            queryWrapper.in(PostEntity::getCategoryId, categoryIds);
        }
        
        return postRepository.selectPage(pageQuery, queryWrapper);
    }
    
    /**
     * 根据ID获取已发布的公开文章
     * 只返回已发布状态的文章
     * 
     * @param postId 文章ID
     * @return 文章实体
     * @throws BusinessException 如果文章不存在或未发布
     */
    public PostEntity getAppPostById(String postId) {
        PostEntity post = postRepository.selectOne(
            new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .eq(PostEntity::getStatus, PostStatus.PUBLISHED)
        );
        
        if (post == null) {
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        return post;
    }
}
