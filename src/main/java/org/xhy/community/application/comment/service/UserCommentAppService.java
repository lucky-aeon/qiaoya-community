package org.xhy.community.application.comment.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.comment.assembler.CommentAssembler;
import org.xhy.community.application.comment.dto.CommentDTO;
import org.xhy.community.application.comment.dto.LatestCommentDTO;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.comment.service.CommentDomainService;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.post.service.PostDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.interfaces.comment.request.CreateReplyCommentRequest;
import org.xhy.community.interfaces.comment.request.CommentQueryRequest;
import org.xhy.community.interfaces.comment.request.CreateCommentRequest;
import org.xhy.community.interfaces.comment.request.BusinessCommentQueryRequest;
import org.xhy.community.domain.comment.query.CommentQuery;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserCommentAppService {

    private final CommentDomainService commentDomainService;
    private final UserDomainService userDomainService;
    private final PostDomainService postDomainService;
    private final CourseDomainService courseDomainService;
    private final ChapterDomainService chapterDomainService;

    public UserCommentAppService(CommentDomainService commentDomainService,
                                UserDomainService userDomainService,
                                PostDomainService postDomainService,
                                CourseDomainService courseDomainService,
                                ChapterDomainService chapterDomainService) {
        this.commentDomainService = commentDomainService;
        this.userDomainService = userDomainService;
        this.postDomainService = postDomainService;
        this.courseDomainService = courseDomainService;
        this.chapterDomainService = chapterDomainService;
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
        CommentQuery query = new CommentQuery(request.getPageNum(), request.getPageSize());
        query.setUserId(userId);
        
        IPage<CommentEntity> commentPage = commentDomainService.getUserRelatedComments(query);
        
        return commentPage.convert(this::convertCommentEntityToDTO);
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
            userIds = List.of(dto.getCommentUserId(), dto.getReplyUserId())
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        }
        
        if (!userIds.isEmpty()) {
            Map<String, UserEntity> userMap = userDomainService.getUserEntityMapByIds(userIds);

            UserEntity commentUser = userMap.get(dto.getCommentUserId());
            if (commentUser != null) {
                dto.setCommentUserName(commentUser.getName());
            }

            if (dto.getReplyUserId() != null) {
                UserEntity replyUser = userMap.get(dto.getReplyUserId());
                if (replyUser != null) {
                    dto.setReplyUserName(replyUser.getName());
                }
            }
        }
    }

    public List<LatestCommentDTO> getLatestComments() {
        List<CommentEntity> comments = commentDomainService.getLatestComments();

        if (comments.isEmpty()) {
            return List.of();
        }

        // 批量查询用户信息（包括评论用户和被回复用户）
        Set<String> userIds = comments.stream()
                .flatMap(comment -> {
                    if (comment.getReplyUserId() != null) {
                        return Stream.of(comment.getCommentUserId(), comment.getReplyUserId());
                    } else {
                        return Stream.of(comment.getCommentUserId());
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, UserEntity> userMap = userDomainService.getUserEntityMapByIds(userIds);

        // 按业务类型分组，分别批量查询文章标题、课程标题和章节标题
        Set<String> postIds = comments.stream()
                .filter(comment -> BusinessType.POST.equals(comment.getBusinessType()))
                .map(CommentEntity::getBusinessId)
                .collect(Collectors.toSet());

        Set<String> courseIds = comments.stream()
                .filter(comment -> BusinessType.COURSE.equals(comment.getBusinessType()))
                .map(CommentEntity::getBusinessId)
                .collect(Collectors.toSet());

        Set<String> chapterIds = comments.stream()
                .filter(comment -> BusinessType.CHAPTER.equals(comment.getBusinessType()))
                .map(CommentEntity::getBusinessId)
                .collect(Collectors.toSet());

        Map<String, String> postTitleMap = postDomainService.getPostTitleMapByIds(postIds);
        Map<String, String> courseTitleMap = courseDomainService.getCourseTitleMapByIds(courseIds);
        Map<String, String> chapterTitleMap = chapterDomainService.getChapterTitleMapByIds(chapterIds);

        return comments.stream()
                .map(comment -> convertToLatestCommentDTO(comment, userMap, postTitleMap, courseTitleMap, chapterTitleMap))
                .collect(Collectors.toList());
    }

    private LatestCommentDTO convertToLatestCommentDTO(CommentEntity comment,
                                                      Map<String, UserEntity> userMap,
                                                      Map<String, String> postTitleMap,
                                                      Map<String, String> courseTitleMap,
                                                      Map<String, String> chapterTitleMap) {
        LatestCommentDTO dto = new LatestCommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCommentUserId(comment.getCommentUserId());
        dto.setReplyUserId(comment.getReplyUserId());
        dto.setBusinessId(comment.getBusinessId());
        dto.setBusinessType(comment.getBusinessType());
        dto.setBusinessTypeName(comment.getBusinessType().getDescription());
        dto.setCreateTime(comment.getCreateTime());

        // 设置评论用户昵称
        UserEntity commentUser = userMap.get(comment.getCommentUserId());
        if (commentUser != null) {
            dto.setCommentUserName(commentUser.getName());
        }

        // 设置被回复用户昵称
        if (comment.getReplyUserId() != null) {
            UserEntity replyUser = userMap.get(comment.getReplyUserId());
            if (replyUser != null) {
                dto.setReplyUserName(replyUser.getName());
            }
        }

        // 设置业务名称
        if (BusinessType.POST.equals(comment.getBusinessType())) {
            dto.setBusinessName(postTitleMap.get(comment.getBusinessId()));
        } else if (BusinessType.COURSE.equals(comment.getBusinessType())) {
            dto.setBusinessName(courseTitleMap.get(comment.getBusinessId()));
        } else if (BusinessType.CHAPTER.equals(comment.getBusinessType())) {
            dto.setBusinessName(chapterTitleMap.get(comment.getBusinessId()));
        }

        return dto;
    }
}