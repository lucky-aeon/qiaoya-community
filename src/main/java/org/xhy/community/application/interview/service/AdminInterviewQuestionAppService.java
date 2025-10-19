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
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.interfaces.interview.request.AdminBatchCreateInterviewQuestionRequest;
import org.xhy.community.domain.interview.valueobject.ProblemStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminInterviewQuestionAppService {

    private final InterviewQuestionDomainService interviewQuestionDomainService;
    private final CategoryDomainService categoryDomainService;
    private final UserDomainService userDomainService;

    public AdminInterviewQuestionAppService(InterviewQuestionDomainService interviewQuestionDomainService,
                                            CategoryDomainService categoryDomainService,
                                            UserDomainService userDomainService) {
        this.interviewQuestionDomainService = interviewQuestionDomainService;
        this.categoryDomainService = categoryDomainService;
        this.userDomainService = userDomainService;
    }

    public InterviewQuestionDTO getById(String id) {
        InterviewQuestionEntity entity = interviewQuestionDomainService.getById(id);
        return InterviewQuestionAssembler.toDTO(entity);
    }

    public IPage<InterviewQuestionDTO> queryQuestions(InterviewQuestionQueryRequest request) {
        var query = InterviewQuestionAssembler.fromAdminQueryRequest(request);
        IPage<InterviewQuestionEntity> entityPage = interviewQuestionDomainService.queryQuestions(query);

        List<InterviewQuestionEntity> records = entityPage.getRecords();
        if (records.isEmpty()) {
            Page<InterviewQuestionDTO> empty = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
            empty.setRecords(List.of());
            return empty;
        }

        Set<String> authorIds = records.stream().map(InterviewQuestionEntity::getAuthorId).collect(Collectors.toSet());
        Set<String> categoryIds = records.stream().map(InterviewQuestionEntity::getCategoryId).collect(Collectors.toSet());

        Map<String, UserEntity> authorMap = userDomainService.getUserEntityMapByIds(authorIds);
        Map<String, String> categoryNames = categoryDomainService.getCategoryNameMapByIds(categoryIds);

        List<InterviewQuestionDTO> dtos = records.stream()
                .map(e -> InterviewQuestionAssembler.toDTO(e, authorMap, categoryNames))
                .toList();

        Page<InterviewQuestionDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtos);
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
    public void delete(String id) {
        interviewQuestionDomainService.delete(id, null, AccessLevel.ADMIN);
    }

    /**
     * 批量创建面试题（已发布），入参仅标题与分类
     */
    @Transactional(rollbackFor = Exception.class)
    public java.util.List<InterviewQuestionDTO> batchCreatePublished(AdminBatchCreateInterviewQuestionRequest request, String operatorId) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            return java.util.Collections.emptyList();
        }

        java.util.List<InterviewQuestionDTO> result = new java.util.ArrayList<>();
        for (AdminBatchCreateInterviewQuestionRequest.Item item : request.getItems()) {
            // 校验分类有效性
            categoryDomainService.validateInterviewCategory(item.getCategoryId());

            // 组装实体：仅标题+分类，设置作者，默认发布
            var entity = InterviewQuestionAssembler.fromTitleCategory(
                    item.getTitle(), item.getCategoryId(), operatorId, ProblemStatus.PUBLISHED
            );
            // 领域创建（会对 rating 进行兜底为3）
            var created = interviewQuestionDomainService.createQuestion(entity);
            result.add(InterviewQuestionAssembler.toDTO(created));
        }
        return result;
    }
}
