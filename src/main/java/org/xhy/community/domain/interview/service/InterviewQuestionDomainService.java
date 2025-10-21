package org.xhy.community.domain.interview.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.interview.entity.InterviewQuestionEntity;
import org.xhy.community.domain.interview.repository.InterviewQuestionRepository;
import org.xhy.community.domain.interview.valueobject.ProblemStatus;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.InterviewErrorCode;
import org.xhy.community.domain.interview.query.InterviewQuestionQuery;

import java.time.LocalDateTime;

@Service
public class InterviewQuestionDomainService {

    private final InterviewQuestionRepository interviewQuestionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public InterviewQuestionDomainService(InterviewQuestionRepository interviewQuestionRepository,
                                          ApplicationEventPublisher eventPublisher) {
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 创建面试题（默认草稿）。
     * 入参直接使用实体（由 App 层通过 Assembler 组装）。
     */
    public InterviewQuestionEntity createQuestion(InterviewQuestionEntity entity) {
        // 默认难度处理（用户创建时App层已固定为3；此处兜底）
        if (entity.getRating() == null) {
            entity.setRating(3);
        }
        validateRating(entity.getRating());
        // 默认状态
        if (entity.getStatus() == null) {
            entity.setStatus(ProblemStatus.DRAFT);
        }
        interviewQuestionRepository.insert(entity);
        return entity;
    }

    /**
     * 统计自 since 起“已发布”的题目数量。
     * 口径：状态=PUBLISHED 且 publish_time > since（若 since 为空则统计全部已发布）。
     */
    public Long countPublishedSince(LocalDateTime since) {
        return interviewQuestionRepository.selectCount(
                new LambdaQueryWrapper<InterviewQuestionEntity>()
                        .eq(InterviewQuestionEntity::getStatus, ProblemStatus.PUBLISHED)
                        .gt(since != null, InterviewQuestionEntity::getPublishTime, since)
        );
    }

    /**
     * 批量按标题创建题目（默认发布，难度=3，描述/答案为空串）
     * 仅需标题列表与分类ID与作者ID，领域内部处理默认值
     */
    public java.util.List<InterviewQuestionEntity> batchCreateByTitles(java.util.List<String> titles,
                                                                       String categoryId,
                                                                       String authorId) {
        if (titles == null || titles.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.util.List<InterviewQuestionEntity> list = new java.util.ArrayList<>();
        for (String t : titles) {
            if (t == null || t.trim().isEmpty()) continue;
            InterviewQuestionEntity e = new InterviewQuestionEntity();
            e.setTitle(t.trim());
            e.setCategoryId(categoryId);
            e.setAuthorId(authorId);
            e.setDescription("");
            e.setAnswer("");
            e.setRating(3);
            e.setStatus(ProblemStatus.PUBLISHED);
            e.setPublishTime(now);
            list.add(e);
        }
        interviewQuestionRepository.insert(list);

        // 发布内容事件（题目默认已发布时触发通知）
        for (InterviewQuestionEntity e : list) {
            try {
                org.xhy.community.domain.common.event.ContentPublishedEvent event =
                        new org.xhy.community.domain.common.event.ContentPublishedEvent(
                                org.xhy.community.domain.common.valueobject.ContentType.INTERVIEW_QUESTION,
                                e.getId(),
                                e.getAuthorId()
                        );
                eventPublisher.publishEvent(event);
            } catch (Exception ignore) {
                // 事件发布失败不影响主流程
            }
        }
        return list;
    }

    /**
     * 根据ID获取面试题
     */
    public InterviewQuestionEntity getById(String id) {
        InterviewQuestionEntity questionEntity = interviewQuestionRepository.selectOne(
                new LambdaQueryWrapper<InterviewQuestionEntity>()
                        .eq(InterviewQuestionEntity::getId, id)
        );
        if (questionEntity == null) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }
        return questionEntity;
    }

    /**
     * 更新面试题
     *
     * @param entity      面试题实体
     * @param operatorId  操作人ID
     * @param accessLevel 权限级别
     * @return 更新后的实体
     */
    public InterviewQuestionEntity updateQuestion(InterviewQuestionEntity entity, String operatorId, AccessLevel accessLevel) {
        // 用户不允许修改难度：强制忽略
        if (accessLevel == AccessLevel.USER) {
            entity.setRating(null);
        } else {
            if (entity.getRating() != null) {
                validateRating(entity.getRating());
            }
        }

        LambdaUpdateWrapper<InterviewQuestionEntity> wrapper = new LambdaUpdateWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getId, entity.getId());

        if (accessLevel == AccessLevel.USER && operatorId != null) {
            wrapper.eq(InterviewQuestionEntity::getAuthorId, operatorId);
        }

        int updated = interviewQuestionRepository.update(entity, wrapper);
        if (updated == 0) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }

        return entity;
    }

    /**
     * 统一修改面试题状态
     * - PUBLISHED: 设置发布状态并写入 publish_time=now
     * - DRAFT: 撤回为草稿并清空 publish_time
     * - ARCHIVED: 归档
     */
    public InterviewQuestionEntity changeStatus(String id, ProblemStatus targetStatus, String operatorId, AccessLevel accessLevel) {
        LambdaUpdateWrapper<InterviewQuestionEntity> wrapper = new LambdaUpdateWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getId, id);

        if (accessLevel == AccessLevel.USER && operatorId != null) {
            wrapper.eq(InterviewQuestionEntity::getAuthorId, operatorId);
        }
        wrapper.set(InterviewQuestionEntity::getStatus, targetStatus);

        int updated = interviewQuestionRepository.update(null, wrapper);
        if (updated == 0) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }

        InterviewQuestionEntity latest = getById(id);
        // 若设置为发布状态，触发内容发布事件
        if (targetStatus == ProblemStatus.PUBLISHED) {
            try {
                org.xhy.community.domain.common.event.ContentPublishedEvent event =
                        new org.xhy.community.domain.common.event.ContentPublishedEvent(
                                org.xhy.community.domain.common.valueobject.ContentType.INTERVIEW_QUESTION,
                                latest.getId(),
                                latest.getAuthorId()
                        );
                eventPublisher.publishEvent(event);
            } catch (Exception ignore) {
                // 事件发布失败不影响主流程
            }
        }

        return latest;
    }

    /**
     * 删除面试题
     *
     * @param id          面试题ID
     * @param operatorId  操作人ID
     * @param accessLevel 权限级别
     */
    public void delete(String id, String operatorId, AccessLevel accessLevel) {
        LambdaQueryWrapper<InterviewQuestionEntity> wrapper = new LambdaQueryWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getId, id);

        if (accessLevel == AccessLevel.USER && operatorId != null) {
            wrapper.eq(InterviewQuestionEntity::getAuthorId, operatorId);
        }

        int deleted = interviewQuestionRepository.delete(wrapper);
        if (deleted == 0) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }
    }

    /**
     * 分页查询面试题
     *
     * @return 分页结果
     */
    public IPage<InterviewQuestionEntity> queryQuestions(InterviewQuestionQuery query) {
        Page<InterviewQuestionEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        boolean publishedOnly = Boolean.TRUE.equals(query.getPublishedOnly());

        LambdaQueryWrapper<InterviewQuestionEntity> wrapper = new LambdaQueryWrapper<InterviewQuestionEntity>()
                .eq(query.getAccessLevel() == AccessLevel.USER && query.getAuthorId() != null, InterviewQuestionEntity::getAuthorId, query.getAuthorId())
                .eq(query.getCategoryId() != null, InterviewQuestionEntity::getCategoryId, query.getCategoryId())
                // 公开视图强制发布状态，否则使用传入状态条件
                .eq(publishedOnly, InterviewQuestionEntity::getStatus, ProblemStatus.PUBLISHED)
                .eq(!publishedOnly && query.getStatus() != null, InterviewQuestionEntity::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getTitle()), InterviewQuestionEntity::getTitle, query.getTitle())
                .like(StringUtils.hasText(query.getTag()), InterviewQuestionEntity::getTags, query.getTag())
                .ge(query.getMinRating() != null, InterviewQuestionEntity::getRating, query.getMinRating())
                .le(query.getMaxRating() != null, InterviewQuestionEntity::getRating, query.getMaxRating());

        // 排序：公开视图按发布时间，其他按创建时间
        if (publishedOnly) {
            wrapper.orderByDesc(InterviewQuestionEntity::getPublishTime);
        } else {
            wrapper.orderByDesc(InterviewQuestionEntity::getCreateTime);
        }

        return interviewQuestionRepository.selectPage(page, wrapper);
    }

    /**
     * 查询公开题库（已发布的面试题）
     * 供所有用户浏览,不需要登录
     *
     * @return 分页结果
     */
    // 已合并到 queryQuestions，保留方法请删除或改为委派

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(InterviewErrorCode.INVALID_RATING);
        }
    }

    /**
     * 批量查询面试题标题映射
     *
     * @param questionIds 面试题ID集合
     * @return 面试题ID到标题的映射
     */
    public java.util.Map<String, String> getQuestionTitleMapByIds(java.util.Collection<String> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return java.util.Map.of();
        }

        java.util.List<InterviewQuestionEntity> questions = interviewQuestionRepository.selectBatchIds(questionIds);
        return questions.stream()
                .collect(java.util.stream.Collectors.toMap(
                        InterviewQuestionEntity::getId,
                        InterviewQuestionEntity::getTitle
                ));
    }
}
