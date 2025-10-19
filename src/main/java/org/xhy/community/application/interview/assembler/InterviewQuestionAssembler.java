package org.xhy.community.application.interview.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.interview.dto.InterviewQuestionDTO;
import org.xhy.community.domain.interview.entity.InterviewQuestionEntity;
import org.xhy.community.domain.interview.valueobject.ProblemStatus;
import org.xhy.community.interfaces.interview.request.CreateInterviewQuestionRequest;
import org.xhy.community.interfaces.interview.request.UpdateInterviewQuestionRequest;

public class InterviewQuestionAssembler {

    public static InterviewQuestionDTO toDTO(InterviewQuestionEntity entity) {
        if (entity == null) return null;
        InterviewQuestionDTO dto = new InterviewQuestionDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public static InterviewQuestionEntity fromCreateRequest(CreateInterviewQuestionRequest request, String authorId) {
        if (request == null) return null;
        InterviewQuestionEntity entity = new InterviewQuestionEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setAuthorId(authorId);
        entity.setStatus(ProblemStatus.DRAFT);
        return entity;
    }

    public static InterviewQuestionEntity fromUpdateRequest(UpdateInterviewQuestionRequest request, String id) {
        if (request == null) return null;
        InterviewQuestionEntity entity = new InterviewQuestionEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(id);
        return entity;
    }
}
