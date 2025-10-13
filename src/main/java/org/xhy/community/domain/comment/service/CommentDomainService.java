package org.xhy.community.domain.comment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.repository.CommentRepository;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.common.event.ContentPublishedEvent;
import org.xhy.community.domain.common.valueobject.ContentType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CommentErrorCode;
import org.xhy.community.domain.comment.query.CommentQuery;

import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CommentDomainService {
    private static final Logger log = LoggerFactory.getLogger(CommentDomainService.class);

    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CommentDomainService(CommentRepository commentRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.commentRepository = commentRepository;
        this.eventPublisher = eventPublisher;
    }
    
    public CommentEntity createComment(CommentEntity comment) {
        // 如果是回复评论，验证父评论是否存在并设置根评论ID
        if (comment.isReplyComment()) {
            CommentEntity parentComment = getCommentById(comment.getParentCommentId());
            
            // 确定根评论ID
            String rootCommentId = parentComment.isRootComment() ? 
                comment.getParentCommentId() : parentComment.getRootCommentId();
            comment.setRootCommentId(rootCommentId);
        }
        
        commentRepository.insert(comment);

        log.info("【评论】已创建：commentId={}, businessType={}, businessId={}, rootId={}, parentId={}",
                comment.getId(), comment.getBusinessType(), comment.getBusinessId(),
                comment.getRootCommentId(), comment.getParentCommentId());

        // 发布简化的评论创建事件
        publishContentEvent(comment);

        return comment;
    }
    
    public CommentEntity getCommentById(String commentId) {
        CommentEntity comment = commentRepository.selectOne(
            new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getId, commentId)
        );
        
        if (comment == null) {
            log.warn("【评论】未找到：commentId={}", commentId);
            throw new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND);
        }
        
        return comment;
    }
    
    public List<CommentEntity> getRootCommentsByBusiness(String businessId, BusinessType businessType) {
        return commentRepository.selectList(
            new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getBusinessId, businessId)
                .eq(CommentEntity::getBusinessType, businessType)
                .isNull(CommentEntity::getParentCommentId)
                .orderByDesc(CommentEntity::getCreateTime)
        );
    }

    /**
     * 获取指定业务对象的全部评论（包含根评论与回复），按时间升序。
     */
    public List<CommentEntity> getCommentsByBusiness(String businessId, BusinessType businessType) {
        return commentRepository.selectList(
            new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getBusinessId, businessId)
                .eq(CommentEntity::getBusinessType, businessType)
                .orderByAsc(CommentEntity::getCreateTime)
        );
    }
    
    public IPage<CommentEntity> getRootCommentsByBusinessPage(String businessId, BusinessType businessType, 
                                                            Integer pageNum, Integer pageSize) {
        Page<CommentEntity> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<CommentEntity> queryWrapper = new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getBusinessId, businessId)
                .eq(CommentEntity::getBusinessType, businessType)
                .orderByDesc(CommentEntity::getCreateTime);
        
        return commentRepository.selectPage(page, queryWrapper);
    }
    
    public List<CommentEntity> getReplyCommentsByRoot(String rootCommentId) {
        return commentRepository.selectList(
            new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getRootCommentId, rootCommentId)
                .isNotNull(CommentEntity::getParentCommentId)
                .orderByAsc(CommentEntity::getCreateTime)
        );
    }
    
    public List<CommentEntity> getReplyCommentsByParent(String parentCommentId) {
        return commentRepository.selectList(
            new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getParentCommentId, parentCommentId)
                .orderByAsc(CommentEntity::getCreateTime)
        );
    }
    
    public void deleteComment(String commentId, String userId) {
        CommentEntity comment = getCommentById(commentId);
        
        if (!comment.getCommentUserId().equals(userId)) {
            log.warn("【评论】删除未授权：commentId={}, operatorId={}", commentId, userId);
            throw new BusinessException(CommentErrorCode.UNAUTHORIZED_DELETE);
        }
        
        commentRepository.deleteById(commentId);
        log.info("【评论】已删除：commentId={}, operatorId={}", commentId, userId);
    }
    
    public CommentEntity updateComment(CommentEntity comment) {
        commentRepository.updateById(comment);
        log.info("【评论】已更新：commentId={}", comment.getId());
        return comment;
    }
    
    public Long getCommentCountByBusiness(String businessId, BusinessType businessType) {
        return commentRepository.selectCount(
            new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getBusinessId, businessId)
                .eq(CommentEntity::getBusinessType, businessType)
        );
    }
    
    public Long getRootCommentCountByBusiness(String businessId, BusinessType businessType) {
        return commentRepository.selectCount(
            new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getBusinessId, businessId)
                .eq(CommentEntity::getBusinessType, businessType)
                .isNull(CommentEntity::getParentCommentId)
        );
    }
    
    public Long getReplyCountByRoot(String rootCommentId) {
        return commentRepository.selectCount(
            new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getRootCommentId, rootCommentId)
                .isNotNull(CommentEntity::getParentCommentId)
        );
    }

    /**
     * 批量获取业务对象（文章/课程/章节）的评论数量映射
     * 使用分组统计避免 N+1 查询
     */
    public Map<String, Long> getCommentCountMapByBusinessIds(Collection<String> businessIds, BusinessType businessType) {
        if (businessIds == null || businessIds.isEmpty() || businessType == null) {
            return Map.of();
        }

        // 直接查出记录，编码层面分组统计，避免 selectMaps 与全局 MapTypeHandler 的冲突
        List<CommentEntity> list = commentRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CommentEntity>()
                        .select(CommentEntity::getBusinessId) // 仅取业务ID即可
                        .in(CommentEntity::getBusinessId, businessIds)
                        .eq(CommentEntity::getBusinessType, businessType)
        );

        Map<String, Long> counts = list.stream()
                .map(CommentEntity::getBusinessId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        // 确保所有传入ID都有键，缺省为0
        java.util.HashMap<String, Long> result = new java.util.HashMap<>();
        for (String id : businessIds) {
            result.put(id, counts.getOrDefault(id, 0L));
        }
        return result;
    }
    
    public IPage<CommentEntity> getUserRelatedComments(CommentQuery query) {
        Page<CommentEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        // 查询与用户相关的评论：用户发表的评论 + 回复给用户的评论
        LambdaQueryWrapper<CommentEntity> queryWrapper = new LambdaQueryWrapper<CommentEntity>()
                .and(wrapper -> wrapper
                    .eq(CommentEntity::getCommentUserId, query.getUserId())  // 用户发表的评论
                    .or()
                    .eq(CommentEntity::getReplyUserId, query.getUserId())    // 回复给用户的评论
                )
                .orderByDesc(CommentEntity::getCreateTime);

        return commentRepository.selectPage(page, queryWrapper);
    }

    public List<CommentEntity> getLatestComments() {
        return commentRepository.selectList(
            new LambdaQueryWrapper<CommentEntity>()
                .orderByDesc(CommentEntity::getCreateTime)
                .last("LIMIT 5")
        );
    }

    /**
     * 发布简化的评论内容事件
     * 只包含必要的标识信息，由Application层统一处理通知逻辑
     */
    private void publishContentEvent(CommentEntity comment) {
        try {
            ContentPublishedEvent event = new ContentPublishedEvent(
                ContentType.COMMENT,
                comment.getId(),
                comment.getCommentUserId()
            );
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            // 事件发布失败不应影响主业务流程
        }
    }
}
