package org.xhy.community.domain.interview.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.interview.entity.InterviewQuestionEntity;
import org.xhy.community.domain.interview.repository.InterviewQuestionRepository;
import org.xhy.community.domain.interview.valueobject.ProblemStatus;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.InterviewErrorCode;

import java.time.LocalDateTime;

@Service
public class InterviewQuestionDomainService {

    private final InterviewQuestionRepository interviewQuestionRepository;

    public InterviewQuestionDomainService(InterviewQuestionRepository interviewQuestionRepository) {
        this.interviewQuestionRepository = interviewQuestionRepository;
    }

    /**
     * 创建面试题（默认草稿）。
     * 入参直接使用实体（由 App 层通过 Assembler 组装）。
     */
    public InterviewQuestionEntity createQuestion(InterviewQuestionEntity entity) {

        validateRating(entity.getRating());
        // 默认状态
        if (entity.getStatus() == null) {
            entity.setStatus(ProblemStatus.DRAFT);
        }
        interviewQuestionRepository.insert(entity);
        return entity;
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
        if (entity.getRating() != null) {
            validateRating(entity.getRating());
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
     * 发布面试题
     *
     * @param id          面试题ID
     * @param operatorId  操作人ID
     * @param accessLevel 权限级别
     */
    public void publish(String id, String operatorId, AccessLevel accessLevel) {
        LambdaUpdateWrapper<InterviewQuestionEntity> wrapper = new LambdaUpdateWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getId, id)
                .ne(InterviewQuestionEntity::getStatus, ProblemStatus.PUBLISHED);

        if (accessLevel == AccessLevel.USER && operatorId != null) {
            wrapper.eq(InterviewQuestionEntity::getAuthorId, operatorId);
        }

        wrapper.set(InterviewQuestionEntity::getStatus, ProblemStatus.PUBLISHED)
                .set(InterviewQuestionEntity::getPublishTime, LocalDateTime.now());

        int updated = interviewQuestionRepository.update(null, wrapper);
        if (updated == 0) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }
    }

    /**
     * 归档面试题
     *
     * @param id          面试题ID
     * @param operatorId  操作人ID
     * @param accessLevel 权限级别
     */
    public void archive(String id, String operatorId, AccessLevel accessLevel) {
        LambdaUpdateWrapper<InterviewQuestionEntity> wrapper = new LambdaUpdateWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getId, id)
                .ne(InterviewQuestionEntity::getStatus, ProblemStatus.ARCHIVED);

        if (accessLevel == AccessLevel.USER && operatorId != null) {
            wrapper.eq(InterviewQuestionEntity::getAuthorId, operatorId);
        }

        wrapper.set(InterviewQuestionEntity::getStatus, ProblemStatus.ARCHIVED);

        int updated = interviewQuestionRepository.update(null, wrapper);
        if (updated == 0) {
            throw new BusinessException(InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND);
        }
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
     * @param authorId    作者ID（可选）
     * @param categoryId  分类ID（可选）
     * @param status      题目状态（可选）
     * @param keyword     标题搜索关键词（可选）
     * @param tag         标签筛选（可选）
     * @param minRating   最小难度（可选）
     * @param maxRating   最大难度（可选）
     * @param pageNum     页码
     * @param pageSize    每页数量
     * @param accessLevel 权限级别
     * @return 分页结果
     */
    public IPage<InterviewQuestionEntity> queryQuestions(String authorId, String categoryId,
                                                         ProblemStatus status, String keyword, String tag,
                                                         Integer minRating, Integer maxRating,
                                                         Integer pageNum, Integer pageSize, AccessLevel accessLevel) {
        Page<InterviewQuestionEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<InterviewQuestionEntity> queryWrapper = new LambdaQueryWrapper<InterviewQuestionEntity>()
                .eq(accessLevel == AccessLevel.USER && authorId != null, InterviewQuestionEntity::getAuthorId, authorId)
                .eq(categoryId != null, InterviewQuestionEntity::getCategoryId, categoryId)
                .eq(status != null, InterviewQuestionEntity::getStatus, status)
                .like(StringUtils.hasText(keyword), InterviewQuestionEntity::getTitle, keyword)
                .like(StringUtils.hasText(tag), InterviewQuestionEntity::getTags, tag)
                .ge(minRating != null, InterviewQuestionEntity::getRating, minRating)
                .le(maxRating != null, InterviewQuestionEntity::getRating, maxRating)
                .orderByDesc(InterviewQuestionEntity::getCreateTime);

        return interviewQuestionRepository.selectPage(page, queryWrapper);
    }

    /**
     * 查询公开题库（已发布的面试题）
     * 供所有用户浏览,不需要登录
     *
     * @param categoryId 分类ID（可选）
     * @param keyword    标题搜索关键词（可选）
     * @param tag        标签筛选（可选）
     * @param minRating  最小难度（可选）
     * @param maxRating  最大难度（可选）
     * @param pageNum    页码
     * @param pageSize   每页数量
     * @return 分页结果
     */
    public IPage<InterviewQuestionEntity> queryPublicQuestions(String categoryId, String keyword, String tag,
                                                               Integer minRating, Integer maxRating,
                                                               Integer pageNum, Integer pageSize) {
        Page<InterviewQuestionEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<InterviewQuestionEntity> queryWrapper = new LambdaQueryWrapper<InterviewQuestionEntity>()
                .eq(InterviewQuestionEntity::getStatus, ProblemStatus.PUBLISHED)
                .eq(categoryId != null, InterviewQuestionEntity::getCategoryId, categoryId)
                .like(StringUtils.hasText(keyword), InterviewQuestionEntity::getTitle, keyword)
                .like(StringUtils.hasText(tag), InterviewQuestionEntity::getTags, tag)
                .ge(minRating != null, InterviewQuestionEntity::getRating, minRating)
                .le(maxRating != null, InterviewQuestionEntity::getRating, maxRating)
                .orderByDesc(InterviewQuestionEntity::getPublishTime);

        return interviewQuestionRepository.selectPage(page, queryWrapper);
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(InterviewErrorCode.INVALID_RATING);
        }
    }
}
