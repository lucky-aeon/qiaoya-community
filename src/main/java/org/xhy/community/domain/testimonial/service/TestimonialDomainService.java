package org.xhy.community.domain.testimonial.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.testimonial.entity.TestimonialEntity;
import org.xhy.community.domain.testimonial.repository.TestimonialRepository;
import org.xhy.community.domain.testimonial.valueobject.TestimonialStatus;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.TestimonialErrorCode;

import org.xhy.community.domain.testimonial.query.TestimonialQuery;

import java.util.List;

@Service
public class TestimonialDomainService {

    private final TestimonialRepository testimonialRepository;

    public TestimonialDomainService(TestimonialRepository testimonialRepository) {
        this.testimonialRepository = testimonialRepository;
    }

    public void checkUserCanCreateTestimonial(String userId) {
        TestimonialEntity existing = testimonialRepository.selectOne(
            new LambdaQueryWrapper<TestimonialEntity>()
                .eq(TestimonialEntity::getUserId, userId)
        );

        if (existing != null) {
            throw new BusinessException(TestimonialErrorCode.USER_ALREADY_SUBMITTED);
        }
    }

    public TestimonialEntity createTestimonial(String userId, String content, Integer rating) {
        if (!StringUtils.hasText(content)) {
            throw new BusinessException(TestimonialErrorCode.CONTENT_EMPTY);
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(TestimonialErrorCode.INVALID_RATING);
        }

        checkUserCanCreateTestimonial(userId);

        try {
            TestimonialEntity testimonial = new TestimonialEntity(userId, content.trim(), rating);
            testimonialRepository.insert(testimonial);
            return testimonial;
        } catch (DataIntegrityViolationException e) {
            // 并发重复提交导致唯一约束异常，转为业务语义
            throw new BusinessException(TestimonialErrorCode.USER_ALREADY_SUBMITTED);
        }
    }

    public TestimonialEntity getUserTestimonial(String userId) {
        return testimonialRepository.selectOne(
            new LambdaQueryWrapper<TestimonialEntity>()
                .eq(TestimonialEntity::getUserId, userId)
        );
    }

    public TestimonialEntity getTestimonialById(String testimonialId) {
        TestimonialEntity testimonial = testimonialRepository.selectOne(
            new LambdaQueryWrapper<TestimonialEntity>()
                .eq(TestimonialEntity::getId, testimonialId)
        );

        if (testimonial == null) {
            throw new BusinessException(TestimonialErrorCode.TESTIMONIAL_NOT_FOUND);
        }

        return testimonial;
    }

    public TestimonialEntity updateTestimonialIfPending(String testimonialId, String userId,
                                                      String content, Integer rating) {
        TestimonialEntity testimonial = getTestimonialById(testimonialId);

        if (!testimonial.getUserId().equals(userId)) {
            throw new BusinessException(TestimonialErrorCode.UNAUTHORIZED_MODIFY);
        }

        if (!testimonial.canBeModified()) {
            throw new BusinessException(TestimonialErrorCode.TESTIMONIAL_NOT_MODIFIABLE);
        }

        if (!StringUtils.hasText(content)) {
            throw new BusinessException(TestimonialErrorCode.CONTENT_EMPTY);
        }

        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(TestimonialErrorCode.INVALID_RATING);
        }

        testimonial.updateContent(content.trim(), rating);
        testimonialRepository.updateById(testimonial);
        return testimonial;
    }

    /**
     * 统一更新路径：使用实体进行更新（仅允许内容与评分变更）
     */
    public TestimonialEntity updateTestimonialIfPending(TestimonialEntity updated, String userId) {
        TestimonialEntity testimonial = getTestimonialById(updated.getId());

        if (!testimonial.getUserId().equals(userId)) {
            throw new BusinessException(TestimonialErrorCode.UNAUTHORIZED_MODIFY);
        }

        if (!testimonial.canBeModified()) {
            throw new BusinessException(TestimonialErrorCode.TESTIMONIAL_NOT_MODIFIABLE);
        }

        if (!StringUtils.hasText(updated.getContent())) {
            throw new BusinessException(TestimonialErrorCode.CONTENT_EMPTY);
        }

        Integer rating = updated.getRating();
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException(TestimonialErrorCode.INVALID_RATING);
        }

        testimonial.updateContent(updated.getContent().trim(), rating);
        testimonialRepository.updateById(testimonial);
        return testimonial;
    }

    public TestimonialEntity changeStatus(String testimonialId, TestimonialStatus newStatus) {
        TestimonialEntity testimonial = getTestimonialById(testimonialId);

        switch (newStatus) {
            case APPROVED:
                testimonial.approve();
                break;
            case REJECTED:
                testimonial.reject();
                break;
            case PUBLISHED:
                if (testimonial.getStatus() != TestimonialStatus.APPROVED) {
                    throw new BusinessException(TestimonialErrorCode.INVALID_STATUS_TRANSITION);
                }
                testimonial.publish();
                break;
            default:
                throw new BusinessException(TestimonialErrorCode.INVALID_STATUS_TRANSITION);
        }

        testimonialRepository.updateById(testimonial);
        return testimonial;
    }

    public List<TestimonialEntity> getPublishedTestimonials() {
        return testimonialRepository.selectList(
            new LambdaQueryWrapper<TestimonialEntity>()
                .eq(TestimonialEntity::getStatus, TestimonialStatus.PUBLISHED)
                .orderByDesc(TestimonialEntity::getSortOrder)
                .orderByDesc(TestimonialEntity::getCreateTime)
        );
    }

    public IPage<TestimonialEntity> getTestimonials(TestimonialQuery query) {
        Page<TestimonialEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<TestimonialEntity> queryWrapper = new LambdaQueryWrapper<TestimonialEntity>()
                .eq(query.getAccessLevel() == AccessLevel.USER && query.getCurrentUserId() != null,
                    TestimonialEntity::getUserId, query.getCurrentUserId())
                .eq(query.getStatus() != null, TestimonialEntity::getStatus, query.getStatus())
                .orderByDesc(TestimonialEntity::getSortOrder)
                .orderByDesc(TestimonialEntity::getCreateTime);

        return testimonialRepository.selectPage(page, queryWrapper);
    }

    public void deleteTestimonial(String testimonialId) {
        TestimonialEntity testimonial = getTestimonialById(testimonialId);
        testimonialRepository.deleteById(testimonialId);
    }

    public TestimonialEntity updateSortOrder(String testimonialId, Integer sortOrder) {
        TestimonialEntity testimonial = getTestimonialById(testimonialId);
        testimonial.setSortOrder(sortOrder);
        testimonialRepository.updateById(testimonial);
        return testimonial;
    }
}
