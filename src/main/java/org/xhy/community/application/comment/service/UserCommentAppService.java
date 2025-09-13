package org.xhy.community.application.comment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.comment.assembler.CommentAssembler;
import org.xhy.community.application.comment.dto.CommentDTO;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.service.CommentDomainService;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.interfaces.comment.request.CreateReplyCommentRequest;
import org.xhy.community.interfaces.comment.request.CommentQueryRequest;
import org.xhy.community.interfaces.comment.request.CreateCommentRequest;
import org.xhy.community.interfaces.comment.request.BusinessCommentQueryRequest;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserCommentAppService {
    
    private final CommentDomainService commentDomainService;
    private final UserDomainService userDomainService;
    
    public UserCommentAppService(CommentDomainService commentDomainService, UserDomainService userDomainService) {
        this.commentDomainService = commentDomainService;
        this.userDomainService = userDomainService;
    }
    
    public CommentDTO createComment(CreateCommentRequest request, String userId) {
        CommentEntity comment = CommentAssembler.fromCreateRequest(request, userId);
        
        CommentEntity createdComment = commentDomainService.createComment(comment);
        
        return CommentAssembler.toDTO(createdComment);
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
    
    public IPage<CommentDTO> getBusinessComments(BusinessCommentQueryRequest request) {
        IPage<CommentEntity> commentPage = commentDomainService.getRootCommentsByBusinessPage(
            request.getBusinessId(), request.getBusinessType(), 
            request.getPageNum(), request.getPageSize()
        );
        
        return commentPage.convert(this::convertCommentEntityToDTO);
    }
    
    private CommentDTO convertCommentEntityToDTO(CommentEntity entity) {
        CommentDTO dto = CommentAssembler.toDTO(entity);
        fillUserNames(dto);
        return dto;
    }
    
    private void fillUserNames(CommentDTO dto) {
        Set<String> userIds = Set.of(dto.getCommentUserId())
            .stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        if (dto.getReplyUserId() != null) {
            userIds = Set.of(dto.getCommentUserId(), dto.getReplyUserId())
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }
        
        if (!userIds.isEmpty()) {
            Map<String, String> userNameMap = userDomainService.getUserNameMapByIds(userIds);
            dto.setCommentUserName(userNameMap.get(dto.getCommentUserId()));
            if (dto.getReplyUserId() != null) {
                dto.setReplyUserName(userNameMap.get(dto.getReplyUserId()));
            }
        }
    }
}