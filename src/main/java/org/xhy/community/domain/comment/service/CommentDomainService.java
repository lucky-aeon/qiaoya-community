package org.xhy.community.domain.comment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.repository.CommentRepository;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CommentErrorCode;

import java.util.List;

@Service
public class CommentDomainService {
    
    private final CommentRepository commentRepository;
    
    public CommentDomainService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
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
        return comment;
    }
    
    public CommentEntity getCommentById(String commentId) {
        CommentEntity comment = commentRepository.selectOne(
            new LambdaQueryWrapper<CommentEntity>()
                .eq(CommentEntity::getId, commentId)
        );
        
        if (comment == null) {
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
            throw new BusinessException(CommentErrorCode.UNAUTHORIZED_DELETE);
        }
        
        commentRepository.deleteById(commentId);
    }
    
    public CommentEntity updateComment(CommentEntity comment) {
        commentRepository.updateById(comment);
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
    
    public IPage<CommentEntity> getUserRelatedComments(String userId, Integer pageNum, Integer pageSize) {
        Page<CommentEntity> page = new Page<>(pageNum, pageSize);
        
        // 查询与用户相关的评论：用户发表的评论 + 回复给用户的评论
        LambdaQueryWrapper<CommentEntity> queryWrapper = new LambdaQueryWrapper<CommentEntity>()
                .and(wrapper -> wrapper
                    .eq(CommentEntity::getCommentUserId, userId)  // 用户发表的评论
                    .or()
                    .eq(CommentEntity::getReplyUserId, userId)    // 回复给用户的评论
                )
                .orderByDesc(CommentEntity::getCreateTime);
        
        return commentRepository.selectPage(page, queryWrapper);
    }
}