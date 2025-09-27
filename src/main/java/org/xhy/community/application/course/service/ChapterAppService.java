package org.xhy.community.application.course.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.ChapterAssembler;
import org.xhy.community.application.course.dto.FrontChapterDetailDTO;
import org.xhy.community.application.course.dto.LatestChapterDTO;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CourseErrorCode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 前台章节应用服务
 * 提供面向前台用户的章节查询功能
 */
@Service
public class ChapterAppService {

    private final ChapterDomainService chapterDomainService;
    private final CourseDomainService courseDomainService;
    private final UserDomainService userDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;

    public ChapterAppService(ChapterDomainService chapterDomainService,
                            CourseDomainService courseDomainService,
                            UserDomainService userDomainService,
                            SubscriptionDomainService subscriptionDomainService,
                            SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.chapterDomainService = chapterDomainService;
        this.courseDomainService = courseDomainService;
        this.userDomainService = userDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }

    /**
     * 根据章节ID获取章节详情
     * 包含权限验证，确保用户有权限访问该章节
     *
     * @param chapterId 章节ID
     * @param userId 用户ID
     * @return 章节详情信息，包含课程名称
     * @throws BusinessException 当章节不存在或用户无权限访问时
     */
    public FrontChapterDetailDTO getChapterById(String chapterId, String userId) {
        ChapterEntity chapter = chapterDomainService.getChapterById(chapterId);
        CourseEntity course = courseDomainService.getCourseById(chapter.getCourseId());

        // 验证用户是否有权限访问该章节
        validateChapterAccess(chapter.getCourseId(), userId);

        return ChapterAssembler.toFrontDetailDTO(chapter, course.getTitle());
    }

    /**
     * 获取最新的5条课程章节
     *
     * @return 最新章节列表，包含课程名称
     */
    public List<LatestChapterDTO> getLatestChapters() {
        List<ChapterEntity> chapters = chapterDomainService.getLatestChapters();

        if (chapters.isEmpty()) {
            return List.of();
        }

        // 批量查询课程信息
        Set<String> courseIds = chapters.stream()
                .map(ChapterEntity::getCourseId)
                .collect(Collectors.toSet());
        Map<String, String> courseTitleMap = courseDomainService.getCourseTitleMapByIds(courseIds);

        return chapters.stream()
                .map(chapter -> convertToLatestChapterDTO(chapter, courseTitleMap))
                .collect(Collectors.toList());
    }

    private LatestChapterDTO convertToLatestChapterDTO(ChapterEntity chapter, Map<String, String> courseTitleMap) {
        LatestChapterDTO dto = new LatestChapterDTO();
        dto.setId(chapter.getId());
        dto.setTitle(chapter.getTitle());
        dto.setCourseId(chapter.getCourseId());
        dto.setCourseName(courseTitleMap.get(chapter.getCourseId()));
        dto.setSortOrder(chapter.getSortOrder());
        dto.setReadingTime(chapter.getReadingTime());
        dto.setCreateTime(chapter.getCreateTime());

        return dto;
    }

    /**
     * 验证用户是否有权限访问指定课程的章节
     * 检查用户是否通过直接购买或有效套餐解锁了该课程
     *
     * @param courseId 课程ID
     * @param userId 用户ID
     * @throws BusinessException 当用户无权限访问时抛出CHAPTER_ACCESS_DENIED异常
     */
    private void validateChapterAccess(String courseId, String userId) {
        if (userId == null) {
            throw new BusinessException(CourseErrorCode.CHAPTER_ACCESS_DENIED);
        }

        try {
            // 检查用户是否直接拥有该课程
            boolean owned = userDomainService.hasUserCourse(userId, courseId);
            if (owned) {
                return;
            }

            // 检查用户是否通过有效套餐解锁该课程
            List<UserSubscriptionEntity> activeSubscriptions = subscriptionDomainService.getUserActiveSubscriptions(userId);
            if (activeSubscriptions != null && !activeSubscriptions.isEmpty()) {
                Set<String> planIds = activeSubscriptions.stream()
                        .map(UserSubscriptionEntity::getSubscriptionPlanId)
                        .collect(Collectors.toSet());
                Set<String> planCourseIds = subscriptionPlanDomainService.getCourseIdsByPlanIds(planIds);

                if (planCourseIds.contains(courseId)) {
                    return;
                }
            }

            // 用户既没有直接购买，也没有通过套餐解锁，拒绝访问
            throw new BusinessException(CourseErrorCode.CHAPTER_ACCESS_DENIED);

        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            // 其他异常也视为无权限访问
            throw new BusinessException(CourseErrorCode.CHAPTER_ACCESS_DENIED);
        }
    }
}