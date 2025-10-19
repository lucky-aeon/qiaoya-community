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
import org.xhy.community.interfaces.interview.request.InterviewQuestionQueryRequest;
import org.xhy.community.interfaces.interview.request.UpdateInterviewQuestionRequest;

@Service
public class AdminInterviewQuestionAppService {

    private final InterviewQuestionDomainService interviewQuestionDomainService;
    private final CategoryDomainService categoryDomainService;

    public AdminInterviewQuestionAppService(InterviewQuestionDomainService interviewQuestionDomainService,
                                            CategoryDomainService categoryDomainService) {
        this.interviewQuestionDomainService = interviewQuestionDomainService;
        this.categoryDomainService = categoryDomainService;
    }

    public InterviewQuestionDTO getById(String id) {
        InterviewQuestionEntity entity = interviewQuestionDomainService.getById(id);
        return InterviewQuestionAssembler.toDTO(entity);
    }

    public IPage<InterviewQuestionDTO> queryQuestions(InterviewQuestionQueryRequest request) {
        IPage<InterviewQuestionEntity> entityPage = interviewQuestionDomainService.queryQuestions(
                null,
                request.getCategoryId(),
                request.getStatus(),
                request.getKeyword(),
                request.getTag(),
                request.getMinRating(),
                request.getMaxRating(),
                request.getPageNum(),
                request.getPageSize(),
                AccessLevel.ADMIN
        );

        Page<InterviewQuestionDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(entityPage.getRecords().stream()
                .map(InterviewQuestionAssembler::toDTO)
                .toList());

        return dtoPage;
    }

    @Transactional(rollbackFor = Exception.class)
    public InterviewQuestionDTO update(String id, UpdateInterviewQuestionRequest request) {
        if (request.getCategoryId() != null) {
            categoryDomainService.validateInterviewCategory(request.getCategoryId());
        }

        InterviewQuestionEntity entity = InterviewQuestionAssembler.fromUpdateRequest(request, id);
        InterviewQuestionEntity updated = interviewQuestionDomainService.updateQuestion(entity, null, AccessLevel.ADMIN);
        return InterviewQuestionAssembler.toDTO(updated);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publish(String id) {
        interviewQuestionDomainService.publish(id, null, AccessLevel.ADMIN);
    }

    @Transactional(rollbackFor = Exception.class)
    public void archive(String id) {
        interviewQuestionDomainService.archive(id, null, AccessLevel.ADMIN);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        interviewQuestionDomainService.delete(id, null, AccessLevel.ADMIN);
    }
}

