package org.xhy.community.application.comment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.comment.assembler.CommentAssembler;
import org.xhy.community.application.comment.dto.CommentDTO;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.service.CommentDomainService;
import org.xhy.community.interfaces.comment.request.CreateReplyCommentRequest;
import org.xhy.community.interfaces.comment.request.CommentQueryRequest;

@Service
public class UserCommentAppService {
    
    private final CommentDomainService commentDomainService;
    
    public UserCommentAppService(CommentDomainService commentDomainService) {
        this.commentDomainService = commentDomainService;
    }
    
    public CommentDTO replyComment(CreateReplyCommentRequest request, String userId) {
        CommentEntity comment = CommentAssembler.fromCreateReplyRequest(request, userId);
        
        CommentEntity createdComment = commentDomainService.createComment(comment);
        
        return CommentAssembler.toDTO(createdComment);
    }
    
    public void deleteComment(String commentId, String userId) {
        commentDomainService.deleteComment(commentId, userId);
    }
    
    public IPage<CommentDTO> getUserRelatedComments(CommentQueryRequest request, String userId) {
        IPage<CommentEntity> commentPage = commentDomainService.getUserRelatedComments(
            userId, request.getPageNum(), request.getPageSize()
        );
        
        return commentPage.convert(CommentAssembler::toDTO);
    }
}