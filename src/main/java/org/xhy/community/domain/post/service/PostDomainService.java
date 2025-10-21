package org.xhy.community.domain.post.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.xhy.community.domain.post.valueobject.QAResolveStatus;
import org.xhy.community.domain.post.repository.PostAcceptedCommentRepository;
import org.xhy.community.domain.post.entity.PostAcceptedCommentEntity;
import org.xhy.community.domain.comment.repository.CommentRepository;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.common.event.ContentPublishedEvent;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.domain.post.valueobject.CategoryType;
import org.xhy.community.infrastructure.exception.CommentErrorCode;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PostDomainService {
    private static final Logger log = LoggerFactory.getLogger(PostDomainService.class);
    
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final PostAcceptedCommentRepository postAcceptedCommentRepository;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PostDomainService(PostRepository postRepository,
                             CategoryRepository categoryRepository,
                             PostAcceptedCommentRepository postAcceptedCommentRepository,
                             CommentRepository commentRepository,
                             ApplicationEventPublisher eventPublisher) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.postAcceptedCommentRepository = postAcceptedCommentRepository;
        this.commentRepository = commentRepository;
        this.eventPublisher = eventPublisher;
    }
    
    public PostEntity createPost(PostEntity post) {
        CategoryEntity category = categoryRepository.selectById(post.getCategoryId());
        if (category == null) {
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }

        if (!category.getIsActive()) {
            throw new BusinessException(PostErrorCode.CATEGORY_DISABLED);
        }

        // 如果文章状态为已发布，设置发布时间
        if (post.getStatus() == PostStatus.PUBLISHED) {
            post.setPublishTime(LocalDateTime.now());
        }

        // 根据分类类型，初始化问答解决状态
        if (category.getType() == CategoryType.QA) {
            post.setResolveStatus(QAResolveStatus.UNSOLVED);
        }

        postRepository.insert(post);

        // 如果文章创建时就是发布状态，发布简化的内容发布事件
        if (post.getStatus() == PostStatus.PUBLISHED) {
            publishContentEvent(post);
        }
        // 文章创建为常规流程，无需日志
        return post;
    }

    /**
     * 统计自 since 起“已发布”的文章数量。
     * 口径：状态=PUBLISHED 且 publish_time > since（若 since 为空则统计全部已发布）。
     */
    public Long countPublishedSince(LocalDateTime since) {
        return postRepository.selectCount(
                new LambdaQueryWrapper<PostEntity>()
                        .eq(PostEntity::getStatus, PostStatus.PUBLISHED)
                        .gt(since != null, PostEntity::getPublishTime, since)
        );
    }

    /**
     * 采纳评论（作者可对多条评论采纳）
     */
    public PostEntity acceptComment(String postId, String commentId, String operatorId, AccessLevel accessLevel) {
        // 校验文章归属与类型
        PostEntity post = postRepository.selectOne(
            new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
        );
        if (post == null) {
            log.warn("【文章】采纳失败：文章不存在，postId={}, operatorId={}", postId, operatorId);
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        CategoryEntity category = categoryRepository.selectById(post.getCategoryId());
        if (category == null) {
            log.warn("【文章】采纳失败：分类不存在，postId={}, categoryId={}", postId, post.getCategoryId());
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }
        if (category.getType() != CategoryType.QA) {
            log.warn("【文章】采纳失败：非问答分类，postId={}, categoryId={}", postId, post.getCategoryId());
            throw new BusinessException(PostErrorCode.NOT_QA_CATEGORY);
        }
        if (accessLevel == AccessLevel.USER && !post.getAuthorId().equals(operatorId)) {
            throw new BusinessException(PostErrorCode.UNAUTHORIZED_ACCEPT);
        }

        // 校验评论存在且属于该文章
        CommentEntity comment = commentRepository.selectOne(
            new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getId, commentId)
        );
        if (comment == null) {
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
        }
        if (comment.getBusinessType() != BusinessType.POST || !postId.equals(comment.getBusinessId())) {
            throw new BusinessException(PostErrorCode.COMMENT_NOT_BELONG_POST);
        }

        // 幂等：若已存在则直接返回
        Long exists = postAcceptedCommentRepository.selectCount(
            new LambdaQueryWrapper<PostAcceptedCommentEntity>()
                .eq(PostAcceptedCommentEntity::getPostId, postId)
                .eq(PostAcceptedCommentEntity::getCommentId, commentId)
        );
        if (exists != null && exists > 0) {
            // 幂等无须输出日志
            return post;
        }

        // 插入采纳关系（并发兜底：唯一约束冲突视为已存在）
        try {
            PostAcceptedCommentEntity relation = new PostAcceptedCommentEntity(postId, commentId);
            postAcceptedCommentRepository.insert(relation);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 并发同时采纳造成唯一约束冲突，视为已采纳
        }

        // 首次采纳则设置已解决与时间
        Long count = postAcceptedCommentRepository.selectCount(
            new LambdaQueryWrapper<PostAcceptedCommentEntity>()
                .eq(PostAcceptedCommentEntity::getPostId, postId)
        );
        if (count != null && count == 1) {
            LambdaUpdateWrapper<PostEntity> update = new LambdaUpdateWrapper<PostEntity>()
                    .eq(PostEntity::getId, postId)
                    .set(PostEntity::getResolveStatus, QAResolveStatus.SOLVED)
                    .set(PostEntity::getSolvedAt, LocalDateTime.now());
            postRepository.update(null, update);
            post.setResolveStatus(QAResolveStatus.SOLVED);
            post.setSolvedAt(LocalDateTime.now());
        }

        log.info("【文章】已采纳评论：postId={}, commentId={}, operatorId={}, access={}",
                postId, commentId, operatorId, accessLevel);
        return post;
    }

    /**
     * 撤销采纳
     */
    public PostEntity revokeAcceptance(String postId, String commentId, String operatorId, AccessLevel accessLevel) {
        PostEntity post = postRepository.selectOne(
            new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
        );
        if (post == null) {
            log.warn("【文章】撤销采纳失败：文章不存在，postId={}, operatorId={}", postId, operatorId);
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        if (accessLevel == AccessLevel.USER && !post.getAuthorId().equals(operatorId)) {
            log.warn("【文章】撤销采纳未授权：postId={}, operatorId={}", postId, operatorId);
            throw new BusinessException(PostErrorCode.UNAUTHORIZED_ACCEPT);
        }

        // 删除采纳关系（软删）
        int deleted = postAcceptedCommentRepository.delete(
            new LambdaQueryWrapper<PostAcceptedCommentEntity>()
                .eq(PostAcceptedCommentEntity::getPostId, postId)
                .eq(PostAcceptedCommentEntity::getCommentId, commentId)
        );

        // 幂等：如果没有记录，直接返回当前状态
        if (deleted == 0) {
            // 幂等无须输出日志
            return post;
        }

        // 判断是否还有其他采纳
        Long remain = postAcceptedCommentRepository.selectCount(
            new LambdaQueryWrapper<PostAcceptedCommentEntity>()
                .eq(PostAcceptedCommentEntity::getPostId, postId)
        );
        if (remain == null || remain == 0) {
            LambdaUpdateWrapper<PostEntity> update = new LambdaUpdateWrapper<PostEntity>()
                    .eq(PostEntity::getId, postId)
                    .set(PostEntity::getResolveStatus, QAResolveStatus.UNSOLVED)
                    .set(PostEntity::getSolvedAt, null);
            postRepository.update(null, update);
            post.setResolveStatus(QAResolveStatus.UNSOLVED);
            post.setSolvedAt(null);
        }
        log.info("【文章】已撤销采纳：postId={}, commentId={}, operatorId={}",
                postId, commentId, operatorId);
        return post;
    }

    /**
     * 获取某帖被采纳的评论ID集合
     */
    public java.util.Set<String> getAcceptedCommentIds(String postId) {
        java.util.List<PostAcceptedCommentEntity> list = postAcceptedCommentRepository.selectList(
            new LambdaQueryWrapper<PostAcceptedCommentEntity>()
                .eq(PostAcceptedCommentEntity::getPostId, postId)
        );
        return list.stream().map(PostAcceptedCommentEntity::getCommentId).collect(java.util.stream.Collectors.toSet());
    }

    /**
     * 删除某个帖子的全部采纳记录，并更新帖子状态
     */
    public void removeAllAcceptancesByPostId(String postId) {
        // 删除关系
        postAcceptedCommentRepository.delete(
            new LambdaQueryWrapper<PostAcceptedCommentEntity>()
                .eq(PostAcceptedCommentEntity::getPostId, postId)
        );
        // 将帖子置为未解决
        LambdaUpdateWrapper<PostEntity> update = new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .set(PostEntity::getResolveStatus, QAResolveStatus.UNSOLVED)
                .set(PostEntity::getSolvedAt, null);
        postRepository.update(null, update);
        // 清空采纳为维护性操作，省略日志
    }

    /**
     * 根据评论ID删除采纳记录（用于删除评论的联动），并维护帖子状态
     */
    public void removeAcceptanceByCommentId(String commentId) {
        // 找出涉及到的帖子ID
        java.util.List<PostAcceptedCommentEntity> list = postAcceptedCommentRepository.selectList(
            new LambdaQueryWrapper<PostAcceptedCommentEntity>()
                .eq(PostAcceptedCommentEntity::getCommentId, commentId)
        );
        if (list.isEmpty()) {
            // 无关联记录，省略日志
            return;
        }
        java.util.Set<String> postIds = list.stream().map(PostAcceptedCommentEntity::getPostId).collect(java.util.stream.Collectors.toSet());
        // 删除这些记录
        postAcceptedCommentRepository.delete(
            new LambdaQueryWrapper<PostAcceptedCommentEntity>()
                .eq(PostAcceptedCommentEntity::getCommentId, commentId)
        );
        // 对每个帖子检查是否还有采纳，如果没有则回退为未解决
        for (String pid : postIds) {
            Long remain = postAcceptedCommentRepository.selectCount(
                new LambdaQueryWrapper<PostAcceptedCommentEntity>()
                    .eq(PostAcceptedCommentEntity::getPostId, pid)
            );
            if (remain == null || remain == 0) {
                LambdaUpdateWrapper<PostEntity> update = new LambdaUpdateWrapper<PostEntity>()
                        .eq(PostEntity::getId, pid)
                        .set(PostEntity::getResolveStatus, QAResolveStatus.UNSOLVED)
                        .set(PostEntity::getSolvedAt, null);
                postRepository.update(null, update);
            }
        }
        // 按评论移除采纳为维护性操作，省略日志
    }

    /**
     * 批量获取多个帖的被采纳评论ID映射
     */
    public java.util.Map<String, java.util.Set<String>> getAcceptedCommentIdsMap(java.util.Set<String> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return java.util.Map.of();
        }
        java.util.List<PostAcceptedCommentEntity> list = postAcceptedCommentRepository.selectList(
            new LambdaQueryWrapper<PostAcceptedCommentEntity>()
                .in(PostAcceptedCommentEntity::getPostId, postIds)
        );
        java.util.Map<String, java.util.Set<String>> map = new java.util.HashMap<>();
        for (PostAcceptedCommentEntity e : list) {
            map.computeIfAbsent(e.getPostId(), k -> new java.util.HashSet<>()).add(e.getCommentId());
        }
        return map;
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
            log.warn("【文章】更新失败：分类不存在，postId={}, categoryId={}", post.getId(), post.getCategoryId());
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }
        if (!category.getIsActive()) {
            log.warn("【文章】更新失败：分类未启用，postId={}, categoryId={}", post.getId(), post.getCategoryId());
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
            log.warn("【文章】更新失败：不存在或非作者，postId={}, authorId={}", post.getId(), authorId);
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        log.info("【文章】已更新：postId={}, authorId={}", post.getId(), authorId);
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
            log.warn("【文章】发布失败：文章不存在，postId={}, authorId={}", postId, authorId);
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (post.getStatus() == PostStatus.PUBLISHED) {
            log.warn("【文章】发布失败：已是发布状态，postId={}, authorId={}", postId, authorId);
            throw new BusinessException(PostErrorCode.POST_ALREADY_PUBLISHED);
        }
        
        // 验证分类是否可用
        CategoryEntity category = categoryRepository.selectById(post.getCategoryId());
        if (category == null) {
            log.warn("【文章】发布失败：分类不存在，postId={}, categoryId={}", postId, post.getCategoryId());
            throw new BusinessException(PostErrorCode.CATEGORY_NOT_FOUND);
        }
        if (!category.getIsActive()) {
            log.warn("【文章】发布失败：分类未启用，postId={}, categoryId={}", postId, post.getCategoryId());
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
            log.warn("【文章】发布失败：不存在或非作者，postId={}, authorId={}", postId, authorId);
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        // 更新内存中的实体状态并返回
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishTime(LocalDateTime.now());

        // 发布简化的内容发布事件
        publishContentEvent(post);

        log.info("【文章】已发布：postId={}, authorId={}", postId, authorId);
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
            log.warn("【文章】撤回失败：文章不存在，postId={}, authorId={}", postId, authorId);
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        if (post.getStatus() == PostStatus.DRAFT) {
            log.warn("【文章】撤回失败：已是草稿状态，postId={}, authorId={}", postId, authorId);
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
            log.warn("【文章】撤回失败：不存在或非作者，postId={}, authorId={}", postId, authorId);
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        
        // 更新内存中的实体状态并返回
        post.setStatus(PostStatus.DRAFT);
        post.setPublishTime(null);
        log.info("【文章】已撤回：postId={}, authorId={}", postId, authorId);
        return post;
    }
    
    public void setTopPost(String postId, boolean isTop) {
        PostEntity post = getPostById(postId);
        
        if (post.getStatus() != PostStatus.PUBLISHED) {
            log.warn("【文章】置顶/取消失败：非发布状态，postId={}", postId);
            throw new BusinessException(PostErrorCode.POST_NOT_PUBLISHED);
        }
        
        post.setIsTop(isTop);
        postRepository.updateById(post);
        // 置顶状态调整为常规操作，省略日志
    }
    
    public void incrementViewCount(String postId) {
        // 原子自增，避免并发丢失
        LambdaUpdateWrapper<PostEntity> updateWrapper = new LambdaUpdateWrapper<PostEntity>()
                .eq(PostEntity::getId, postId)
                .setSql("view_count = view_count + 1");
        int updated = postRepository.update(null, updateWrapper);
        if (updated == 0) {
            log.warn("【文章】浏览计数自增失败：postId={}", postId);
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
            log.warn("【文章】评论计数自增失败：postId={}", postId);
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

    /**
     * 批量查询文章实体映射（用于收藏列表等场景）
     *
     * @param postIds 文章ID集合
     * @return 文章ID到实体的映射
     */
    public Map<String, PostEntity> getPostEntityMapByIds(Collection<String> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        List<PostEntity> posts = postRepository.selectBatchIds(postIds);
        return posts.stream()
                .collect(Collectors.toMap(
                        PostEntity::getId,
                        post -> post
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
            log.warn("【文章】删除失败：不存在或非作者，postId={}, authorId={}", postId, authorId);
            throw new BusinessException(PostErrorCode.POST_NOT_FOUND);
        }
        // 删除为常规操作，省略日志
    }
    
    public void updatePostFields(PostEntity post) {
        postRepository.updateById(post);
    }
    
    public IPage<PostEntity> queryAppPosts(PostQuery query) {
        Page<PostEntity> pageQuery = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<PostEntity> queryWrapper = new LambdaQueryWrapper<PostEntity>()
                .eq(PostEntity::getStatus, PostStatus.PUBLISHED)
                .eq(org.springframework.util.StringUtils.hasText(query.getAuthorId()), PostEntity::getAuthorId, query.getAuthorId())
                .eq(org.springframework.util.StringUtils.hasText(query.getCategoryId()), PostEntity::getCategoryId, query.getCategoryId())
                .eq(query.getIsTop() != null, PostEntity::getIsTop, query.getIsTop())
                .like(org.springframework.util.StringUtils.hasText(query.getTitle()), PostEntity::getTitle, query.getTitle())
                .orderByDesc(PostEntity::getCreateTime);

        // 如果指定了分类类型,需要关联查询分类表
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

    /**
     * 发布简化的文章内容事件
     * 只包含必要的标识信息，由Application层统一处理通知逻辑
     */
    private void publishContentEvent(PostEntity post) {
        try {
            ContentPublishedEvent event = new ContentPublishedEvent(
                ContentType.POST,
                post.getId(),
                post.getAuthorId()
            );
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // 事件发布失败不应影响主业务流程
        }
    }
}
