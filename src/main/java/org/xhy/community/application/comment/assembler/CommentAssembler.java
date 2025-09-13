package org.xhy.community.application.comment.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.comment.dto.CommentDTO;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.interfaces.comment.request.CreateReplyCommentRequest;

public class CommentAssembler {
    
    public static CommentDTO toDTO(CommentEntity entity) {
        if (entity == null) {
            return null;
        }
        
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    public static CommentEntity fromCreateReplyRequest(CreateReplyCommentRequest request, String userId) {
        if (request == null) {
            return null;
        }
        
        CommentEntity entity = new CommentEntity();
        entity.setContent(request.getContent());
        entity.setCommentUserId(userId);
        entity.setBusinessId(request.getBusinessId());
        entity.setBusinessType(request.getBusinessType());
        entity.setParentCommentId(request.getParentCommentId());
        entity.setReplyUserId(request.getReplyUserId());
        
        return entity;
    }
}