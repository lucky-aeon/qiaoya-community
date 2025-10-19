package org.xhy.community.application.interview.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.interview.assembler.InterviewQuestionAssembler;
import org.xhy.community.application.interview.dto.InterviewQuestionDTO;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.interview.entity.InterviewQuestionEntity;
import org.xhy.community.domain.interview.service.InterviewQuestionDomainService;
import org.xhy.community.domain.post.service.CategoryDomainService;
import org.xhy.community.interfaces.interview.request.CreateInterviewQuestionRequest;
import org.xhy.community.interfaces.interview.request.InterviewQuestionQueryRequest;
import org.xhy.community.interfaces.interview.request.UpdateInterviewQuestionRequest;
import org.xhy.community.interfaces.interview.request.BatchCreateInterviewQuestionsRequest;
import org.xhy.community.domain.interview.valueobject.ProblemStatus;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.config.ValidationErrorCode;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.application.like.helper.LikeCountHelper;
import org.xhy.community.domain.like.service.LikeDomainService;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.domain.comment.service.CommentDomainService;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.log.service.UserActivityLogDomainService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class InterviewQuestionAppService {

    private final InterviewQuestionDomainService interviewQuestionDomainService;
    private final CategoryDomainService categoryDomainService;
    private final UserDomainService userDomainService;
    private final LikeDomainService likeDomainService;
    private final CommentDomainService commentDomainService;
    private final UserActivityLogDomainService userActivityLogDomainService;

    public InterviewQuestionAppService(InterviewQuestionDomainService interviewQuestionDomainService,
                                       CategoryDomainService categoryDomainService,
                                       UserDomainService userDomainService,
                                       LikeDomainService likeDomainService,
                                       CommentDomainService commentDomainService,
                                       UserActivityLogDomainService userActivityLogDomainService) {
        this.interviewQuestionDomainService = interviewQuestionDomainService;
        this.categoryDomainService = categoryDomainService;
        this.userDomainService = userDomainService;
        this.likeDomainService = likeDomainService;
        this.commentDomainService = commentDomainService;
        this.userActivityLogDomainService = userActivityLogDomainService;
    }

    @Transactional(rollbackFor = Exception.class)
    public InterviewQuestionDTO createQuestion(CreateInterviewQuestionRequest request, String authorId) {
        categoryDomainService.validateInterviewCategory(request.getCategoryId());

        InterviewQuestionEntity entity = InterviewQuestionAssembler.fromCreateRequest(request, authorId);
        // 用户创建：难度固定为3
        entity.setRating(3);
        InterviewQuestionEntity created = interviewQuestionDomainService.createQuestion(entity);
        return InterviewQuestionAssembler.toDTO(created);
    }

    @Transactional(rollbackFor = Exception.class)
    public InterviewQuestionDTO updateQuestion(String id, UpdateInterviewQuestionRequest request, String operatorId) {
        if (request.getCategoryId() != null) {
            categoryDomainService.validateInterviewCategory(request.getCategoryId());
        }

        InterviewQuestionEntity entity = InterviewQuestionAssembler.fromUpdateRequest(request, id);
        // 用户更新：不允许修改难度
        entity.setRating(null);
        InterviewQuestionEntity updated = interviewQuestionDomainService.updateQuestion(entity, operatorId, AccessLevel.USER);
        return InterviewQuestionAssembler.toDTO(updated);
    }

    public InterviewQuestionDTO getById(String id) {
        InterviewQuestionEntity entity = interviewQuestionDomainService.getById(id);

        // 组装作者名与分类名
        java.util.Map<String, UserEntity> authorMap = userDomainService.getUserEntityMapByIds(
                java.util.Collections.singleton(entity.getAuthorId()));
        java.util.Map<String, String> categoryNames = categoryDomainService.getCategoryNameMapByIds(
                java.util.Collections.singleton(entity.getCategoryId()));

        InterviewQuestionDTO dto = InterviewQuestionAssembler.toDTO(entity, authorMap, categoryNames);

        // 点赞数
        dto.setLikeCount(LikeCountHelper.getLikeCount(id, LikeTargetType.INTERVIEW_QUESTION, likeDomainService));
        // 评论数（全部评论）
        Long c = commentDomainService.getCommentCountByBusiness(id, BusinessType.INTERVIEW_QUESTION);
        dto.setCommentCount(c == null ? 0 : c.intValue());
        // 阅读数（按用户去重）
        Long v = userActivityLogDomainService.getDistinctViewerCountByInterviewQuestionId(id);
        dto.setViewCount(v == null ? 0 : v.intValue());

        return dto;
    }

    public IPage<InterviewQuestionDTO> queryQuestions(InterviewQuestionQueryRequest request, String userId) {
        var query = InterviewQuestionAssembler.fromUserQueryRequest(request, userId);
        IPage<InterviewQuestionEntity> entityPage = interviewQuestionDomainService.queryQuestions(query);

        Page<InterviewQuestionDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream()
                .map(InterviewQuestionAssembler::toDTO)
                .toList());

        return dtoPage;
    }

    public IPage<InterviewQuestionDTO> queryPublicQuestions(InterviewQuestionQueryRequest request) {
        var query = InterviewQuestionAssembler.fromPublicQueryRequest(request);
        IPage<InterviewQuestionEntity> entityPage = interviewQuestionDomainService.queryQuestions(query);

        List<InterviewQuestionEntity> records = entityPage.getRecords();
        if (records.isEmpty()) {
            Page<InterviewQuestionDTO> empty = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
            empty.setRecords(List.of());
            return empty;
        }

        // 批量查询作者与分类名称，避免N+1
        Set<String> authorIds = records.stream().map(InterviewQuestionEntity::getAuthorId).collect(Collectors.toSet());
        Set<String> categoryIds = records.stream().map(InterviewQuestionEntity::getCategoryId).collect(Collectors.toSet());

        Map<String, UserEntity> authorMap = userDomainService.getUserEntityMapByIds(authorIds);
        Map<String, String> categoryNames = categoryDomainService.getCategoryNameMapByIds(categoryIds);

        // 组装基础DTO并带上作者/分类名
        List<InterviewQuestionDTO> dtos = records.stream()
                .map(e -> InterviewQuestionAssembler.toDTO(e, authorMap, categoryNames))
                .toList();

        // 填充点赞数
        LikeCountHelper.fillLikeCount(dtos, InterviewQuestionDTO::getId, LikeTargetType.INTERVIEW_QUESTION, InterviewQuestionDTO::setLikeCount, likeDomainService);

        // 填充评论数（按业务类型：INTERVIEW_QUESTION）
        Set<String> ids = records.stream().map(InterviewQuestionEntity::getId).collect(Collectors.toSet());
        Map<String, Long> commentCountMap = commentDomainService.getCommentCountMapByBusinessIds(ids, BusinessType.INTERVIEW_QUESTION);
        dtos.forEach(dto -> dto.setCommentCount(commentCountMap.getOrDefault(dto.getId(), 0L).intValue()));

        // 填充阅读数（按用户去重）
        Map<String, Long> viewCountMap = userActivityLogDomainService.getDistinctViewerCountMapByInterviewQuestionIds(ids);
        dtos.forEach(dto -> dto.setViewCount(viewCountMap.getOrDefault(dto.getId(), 0L).intValue()));

        Page<InterviewQuestionDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }


    @Transactional(rollbackFor = Exception.class)
    public void delete(String id, String operatorId) {
        interviewQuestionDomainService.delete(id, operatorId, AccessLevel.USER);
    }

    /**
     * 用户批量创建面试题（默认已发布）
     * 仅入参标题列表与单一分类ID
     */
    @Transactional(rollbackFor = Exception.class)
    public java.util.List<InterviewQuestionDTO> batchCreatePublished(BatchCreateInterviewQuestionsRequest request, String userId) {
        categoryDomainService.validateInterviewCategory(request.getCategoryId());
        java.util.List<InterviewQuestionEntity> created = interviewQuestionDomainService.batchCreateByTitles(
                request.getTitles(), request.getCategoryId(), userId
        );
        return created.stream().map(InterviewQuestionAssembler::toDTO).toList();
    }

    /**
     * 修改面试题状态（草稿/发布）
     */
    @Transactional(rollbackFor = Exception.class)
    public InterviewQuestionDTO changeStatus(String id, ProblemStatus targetStatus, String operatorId) {
        InterviewQuestionEntity entity = interviewQuestionDomainService.changeStatus(id, targetStatus, operatorId, AccessLevel.USER);
        return InterviewQuestionAssembler.toDTO(entity);
    }
}
