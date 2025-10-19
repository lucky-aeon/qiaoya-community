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

@Service
public class InterviewQuestionAppService {

    private final InterviewQuestionDomainService interviewQuestionDomainService;
    private final CategoryDomainService categoryDomainService;

    public InterviewQuestionAppService(InterviewQuestionDomainService interviewQuestionDomainService,
                                       CategoryDomainService categoryDomainService) {
        this.interviewQuestionDomainService = interviewQuestionDomainService;
        this.categoryDomainService = categoryDomainService;
    }

    @Transactional(rollbackFor = Exception.class)
    public InterviewQuestionDTO createQuestion(CreateInterviewQuestionRequest request, String authorId) {
        categoryDomainService.validateInterviewCategory(request.getCategoryId());

        InterviewQuestionEntity entity = InterviewQuestionAssembler.fromCreateRequest(request, authorId);
        InterviewQuestionEntity created = interviewQuestionDomainService.createQuestion(entity);
        return InterviewQuestionAssembler.toDTO(created);
    }

    @Transactional(rollbackFor = Exception.class)
    public InterviewQuestionDTO updateQuestion(String id, UpdateInterviewQuestionRequest request, String operatorId) {
        if (request.getCategoryId() != null) {
            categoryDomainService.validateInterviewCategory(request.getCategoryId());
        }

        InterviewQuestionEntity entity = InterviewQuestionAssembler.fromUpdateRequest(request, id);
        InterviewQuestionEntity updated = interviewQuestionDomainService.updateQuestion(entity, operatorId, AccessLevel.USER);
        return InterviewQuestionAssembler.toDTO(updated);
    }

    public InterviewQuestionDTO getById(String id) {
        InterviewQuestionEntity entity = interviewQuestionDomainService.getById(id);
        return InterviewQuestionAssembler.toDTO(entity);
    }

    public IPage<InterviewQuestionDTO> queryQuestions(InterviewQuestionQueryRequest request, String userId) {
        IPage<InterviewQuestionEntity> entityPage = interviewQuestionDomainService.queryQuestions(
                userId,
                request.getCategoryId(),
                request.getStatus(),
                request.getKeyword(),
                request.getTag(),
                request.getMinRating(),
                request.getMaxRating(),
                request.getPageNum(),
                request.getPageSize(),
                AccessLevel.USER
        );

        Page<InterviewQuestionDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream()
                .map(InterviewQuestionAssembler::toDTO)
                .toList());

        return dtoPage;
    }

    public IPage<InterviewQuestionDTO> queryPublicQuestions(InterviewQuestionQueryRequest request) {
        IPage<InterviewQuestionEntity> entityPage = interviewQuestionDomainService.queryPublicQuestions(
                request.getCategoryId(),
                request.getKeyword(),
                request.getTag(),
                request.getMinRating(),
                request.getMaxRating(),
                request.getPageNum(),
                request.getPageSize()
        );

        Page<InterviewQuestionDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream()
                .map(InterviewQuestionAssembler::toDTO)
                .toList());

        return dtoPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(String id, String operatorId) {
        interviewQuestionDomainService.publish(id, operatorId, AccessLevel.USER);
    }

    @Transactional(rollbackFor = Exception.class)
    public void archive(String id, String operatorId) {
        interviewQuestionDomainService.archive(id, operatorId, AccessLevel.USER);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String id, String operatorId) {
        interviewQuestionDomainService.delete(id, operatorId, AccessLevel.USER);
    }
}
