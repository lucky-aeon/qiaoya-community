package org.xhy.community.application.interview.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.interview.dto.InterviewQuestionDTO;
import org.xhy.community.domain.interview.entity.InterviewQuestionEntity;
import org.xhy.community.domain.interview.valueobject.ProblemStatus;
import org.xhy.community.interfaces.interview.request.CreateInterviewQuestionRequest;
import org.xhy.community.interfaces.interview.request.UpdateInterviewQuestionRequest;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.interview.query.InterviewQuestionQuery;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.interview.valueobject.ProblemStatus;

import java.util.Map;
import java.time.LocalDateTime;

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

    /**
     * 扩展：带作者/分类名称的DTO转换（用于前台公开列表）
     */
    public static InterviewQuestionDTO toDTO(InterviewQuestionEntity entity,
                                             Map<String, UserEntity> authorMap,
                                             Map<String, String> categoryNames) {
        if (entity == null) return null;
        InterviewQuestionDTO dto = new InterviewQuestionDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setStatus(entity.getStatus());

        if (authorMap != null) {
            UserEntity author = authorMap.get(entity.getAuthorId());
            if (author != null) {
                dto.setAuthorName(author.getName());
            }
        }
        if (categoryNames != null) {
            dto.setCategoryName(categoryNames.get(entity.getCategoryId()));
        }
        return dto;
    }

    // ===== 查询装配 =====
    public static InterviewQuestionQuery fromUserQueryRequest(org.xhy.community.interfaces.interview.request.InterviewQuestionQueryRequest request,
                                                              String authorId) {
        InterviewQuestionQuery query = new InterviewQuestionQuery(request.getPageNum(), request.getPageSize());
        query.setAuthorId(authorId);
        query.setCategoryId(request.getCategoryId());
        query.setStatus(request.getStatus());
        query.setTitle(request.getTitle());
        query.setTag(request.getTag());
        query.setMinRating(request.getMinRating());
        query.setMaxRating(request.getMaxRating());
        query.setAccessLevel(AccessLevel.USER);
        return query;
    }

    public static InterviewQuestionQuery fromPublicQueryRequest(org.xhy.community.interfaces.interview.request.InterviewQuestionQueryRequest request) {
        InterviewQuestionQuery query = new InterviewQuestionQuery(request.getPageNum(), request.getPageSize());
        query.setCategoryId(request.getCategoryId());
        query.setTitle(request.getTitle());
        query.setTag(request.getTag());
        query.setMinRating(request.getMinRating());
        query.setMaxRating(request.getMaxRating());
        query.setPublishedOnly(true);
        return query;
    }

    public static InterviewQuestionQuery fromAdminQueryRequest(org.xhy.community.interfaces.interview.request.InterviewQuestionQueryRequest request) {
        InterviewQuestionQuery query = new InterviewQuestionQuery(request.getPageNum(), request.getPageSize());
        query.setCategoryId(request.getCategoryId());
        query.setStatus(request.getStatus());
        query.setTitle(request.getTitle());
        query.setTag(request.getTag());
        query.setMinRating(request.getMinRating());
        query.setMaxRating(request.getMaxRating());
        query.setAccessLevel(AccessLevel.ADMIN);
        return query;
    }

    /**
     * 仅标题+分类的创建（批量）
     */
    public static InterviewQuestionEntity fromTitleCategory(String title, String categoryId, String authorId, ProblemStatus status) {
        InterviewQuestionEntity entity = new InterviewQuestionEntity();
        entity.setTitle(title);
        entity.setCategoryId(categoryId);
        entity.setAuthorId(authorId);
        entity.setStatus(status);
        // 最小可用字段：描述/答案非空，难度默认3
        entity.setDescription("");
        entity.setAnswer("");
        entity.setRating(3);
        if (status == ProblemStatus.PUBLISHED) {
            entity.setPublishTime(LocalDateTime.now());
        }
        return entity;
    }
}
