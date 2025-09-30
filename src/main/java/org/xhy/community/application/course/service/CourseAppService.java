package org.xhy.community.application.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.CourseAssembler;
import org.xhy.community.application.course.dto.FrontCourseDTO;
import org.xhy.community.application.course.dto.FrontCourseDetailDTO;
import org.xhy.community.application.course.dto.PublicCourseDTO;
import org.xhy.community.application.course.dto.PublicCourseDetailDTO;
import org.xhy.community.application.course.assembler.PublicCourseAssembler;
import org.xhy.community.application.subscription.assembler.SubscriptionPlanAssembler;
import org.xhy.community.application.subscription.dto.AppSubscriptionPlanDTO;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.course.query.CourseQuery;
import org.xhy.community.interfaces.course.request.AppCourseQueryRequest;
// Do not use UserContext in App layer; userId passed from API
import org.xhy.community.domain.like.service.LikeDomainService;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.application.like.helper.LikeCountHelper;
 

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 前台课程应用服务
 * 提供面向前台用户的课程查询功能
 */
@Service
public class CourseAppService {
    
    private final CourseDomainService courseDomainService;
    private final ChapterDomainService chapterDomainService;
    private final UserDomainService userDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final LikeDomainService likeDomainService;
    
    public CourseAppService(CourseDomainService courseDomainService,
                           ChapterDomainService chapterDomainService,
                           UserDomainService userDomainService,
                           SubscriptionDomainService subscriptionDomainService,
                           SubscriptionPlanDomainService subscriptionPlanDomainService,
                           LikeDomainService likeDomainService) {
        this.courseDomainService = courseDomainService;
        this.chapterDomainService = chapterDomainService;
        this.userDomainService = userDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.likeDomainService = likeDomainService;
    }
    
    /**
     * 分页查询前台课程列表
     * 返回所有可见课程（包括更新中/已完成等状态）
     *
     * @param request 查询请求参数
     * @return 课程列表分页结果
     */
    public IPage<FrontCourseDTO> queryAppCourses(AppCourseQueryRequest request, String userId) {
        // 转换查询参数
        CourseQuery query = CourseAssembler.fromAppQueryRequest(request);

        // 查询课程分页数据（不限制为已完成）
        IPage<CourseEntity> coursePage = courseDomainService.getPagedCourses(query);
        
        if (coursePage.getRecords().isEmpty()) {
            // 创建一个空的分页结果
            IPage<FrontCourseDTO> result = coursePage.convert(entity -> null);
            result.setRecords(List.of());
            return result;
        }
        
        // 提取作者ID集合
        Set<String> authorIds = coursePage.getRecords().stream()
                .map(CourseEntity::getAuthorId)
                .collect(Collectors.toSet());
        
        // 批量查询作者信息
        Map<String, UserEntity> authorMap = userDomainService.getUserEntityMapByIds(authorIds);
        
        // 提取课程ID集合
        Set<String> courseIds = coursePage.getRecords().stream()
                .map(CourseEntity::getId)
                .collect(Collectors.toSet());
        
        // 批量查询每个课程的章节数量
        Map<String, Integer> chapterCountMap = getChapterCountMap(courseIds);
        
        // 转换为前台DTO
        List<FrontCourseDTO> frontCourseDTOs = CourseAssembler.toFrontDTOList(
                coursePage.getRecords(),
                authorMap,
                chapterCountMap
        );

        LikeCountHelper.fillLikeCount(frontCourseDTOs, FrontCourseDTO::getId, LikeTargetType.COURSE, FrontCourseDTO::setLikeCount, likeDomainService);

        // 批量设置课程解锁状态
        setCoursesUnlockStatus(frontCourseDTOs, userId);
        
        // 构建结果分页对象
        IPage<FrontCourseDTO> result = coursePage.convert(entity -> (FrontCourseDTO) null);
        result.setRecords(frontCourseDTOs);
        return result;
    }

    /**
     * 公开接口：分页查询课程列表（不返回敏感链接字段）
     */
    public IPage<PublicCourseDTO> queryPublicCourses(AppCourseQueryRequest request) {
        CourseQuery query = CourseAssembler.fromAppQueryRequest(request);

        IPage<CourseEntity> coursePage = courseDomainService.getPagedCourses(query);
        if (coursePage.getRecords().isEmpty()) {
            IPage<PublicCourseDTO> result = coursePage.convert(entity -> null);
            result.setRecords(List.of());
            return result;
        }

        Set<String> courseIds = coursePage.getRecords().stream()
                .map(CourseEntity::getId)
                .collect(java.util.stream.Collectors.toSet());
        Map<String, Integer> chapterCountMap = getChapterCountMap(courseIds);

        List<PublicCourseDTO> dtos = PublicCourseAssembler.toPublicDTOList(
                coursePage.getRecords(), chapterCountMap
        );

        LikeCountHelper.fillLikeCount(dtos, PublicCourseDTO::getId, LikeTargetType.COURSE, PublicCourseDTO::setLikeCount, likeDomainService);

        IPage<PublicCourseDTO> result = coursePage.convert(entity -> (PublicCourseDTO) null);
        result.setRecords(dtos);
        return result;
    }

    /**
     * 公开接口：获取课程详情（不返回敏感链接字段）
     */
    public PublicCourseDetailDTO getPublicCourseDetail(String courseId) {
        CourseEntity course = courseDomainService.getCourseById(courseId);

        List<ChapterEntity> chapters = chapterDomainService.getChaptersByCourseId(courseId);
        PublicCourseDetailDTO dto = PublicCourseAssembler.toPublicDetailDTO(course, chapters);
        dto.setLikeCount(LikeCountHelper.getLikeCount(courseId, LikeTargetType.COURSE, likeDomainService));
        LikeCountHelper.fillLikeCount(dto.getChapters(), PublicCourseDetailDTO.FrontChapterDTO::getId, LikeTargetType.CHAPTER, PublicCourseDetailDTO.FrontChapterDTO::setLikeCount, likeDomainService);
        return dto;
    }

    /**
     * 获取前台课程详情
     * 包含课程信息和章节列表，不限制课程状态
     *
     * @param courseId 课程ID
     * @return 课程详情信息
     */
    public FrontCourseDetailDTO getAppCourseDetail(String courseId, String userId) {
        // 获取课程信息
        CourseEntity course = courseDomainService.getCourseById(courseId);

        // 获取作者信息
        Map<String, UserEntity> authorMap = userDomainService.getUserEntityMapByIds(
                java.util.Collections.singleton(course.getAuthorId())
        );
        UserEntity author = authorMap.get(course.getAuthorId());
        
        // 获取章节列表
        List<ChapterEntity> chapters = chapterDomainService.getChaptersByCourseId(courseId);

        // 转换为前台详情DTO
        FrontCourseDetailDTO dto = CourseAssembler.toFrontDetailDTO(course, author, chapters);

        // 设置课程解锁状态和解锁套餐
        setCourseUnlockStatus(dto, courseId, userId);

        dto.setLikeCount(LikeCountHelper.getLikeCount(courseId, LikeTargetType.COURSE, likeDomainService));

        LikeCountHelper.fillLikeCount(dto.getChapters(), FrontCourseDetailDTO.FrontChapterDTO::getId, LikeTargetType.CHAPTER, FrontCourseDetailDTO.FrontChapterDTO::setLikeCount, likeDomainService);

        return dto;
    }
    
    /**
     * 批量获取课程的章节数量
     *
     * @param courseIds 课程ID集合
     * @return 课程ID -> 章节数量的映射
     */
    private Map<String, Integer> getChapterCountMap(Set<String> courseIds) {
        return courseIds.stream()
                .collect(Collectors.toMap(
                        courseId -> courseId,
                        courseId -> chapterDomainService.getChaptersByCourseId(courseId).size()
                ));
    }

    /**
     * 批量设置课程解锁状态
     * 检查用户是否通过直接购买或有效套餐解锁了课程
     *
     * @param courses 课程DTO列表
     * @param userId 用户ID
     */
    private void setCoursesUnlockStatus(List<FrontCourseDTO> courses, String userId) {
        if (courses == null || courses.isEmpty() || userId == null) {
            // 未登录或空列表，默认未解锁
            courses.forEach(dto -> dto.setUnlocked(false));
            return;
        }

        try {
            // 1) 获取用户直接拥有的课程（购买/授予）
            Set<String> ownedCourseIds = userDomainService.getUserCourses(userId)
                    .stream().collect(Collectors.toSet());

            // 2) 获取用户当前有效订阅所包含的课程
            List<UserSubscriptionEntity> activeSubscriptions = subscriptionDomainService.getUserActiveSubscriptions(userId);
            Set<String> planCourseIds = activeSubscriptions == null || activeSubscriptions.isEmpty()
                    ? Set.of()
                    : subscriptionPlanDomainService.getCourseIdsByPlanIds(
                            activeSubscriptions.stream()
                                    .map(UserSubscriptionEntity::getSubscriptionPlanId)
                                    .collect(Collectors.toSet()));

            // 3) 合并所有已解锁的课程ID
            Set<String> unlockedCourseIds = new java.util.HashSet<>();
            unlockedCourseIds.addAll(ownedCourseIds);
            unlockedCourseIds.addAll(planCourseIds);

            // 4) 设置每个课程的解锁状态
            courses.forEach(dto -> dto.setUnlocked(unlockedCourseIds.contains(dto.getId())));

        } catch (Exception e) {
            // 容错：出现异常不影响主体查询，默认未解锁
            courses.forEach(dto -> dto.setUnlocked(false));
        }
    }

    /**
     * 设置单个课程的解锁状态和解锁套餐信息
     *
     * @param dto 课程详情DTO
     * @param courseId 课程ID
     * @param userId 用户ID
     */
    private void setCourseUnlockStatus(FrontCourseDetailDTO dto, String courseId, String userId) {
        if (userId == null) {
            dto.setUnlocked(false);
            return;
        }

        try {
            // 检查用户是否直接拥有该课程
            boolean owned = userDomainService.hasUserCourse(userId, courseId);

            // 检查用户是否通过有效套餐解锁该课程
            boolean unlockedByPlan = false;
            if (!owned) {
                List<UserSubscriptionEntity> activeSubscriptions = subscriptionDomainService.getUserActiveSubscriptions(userId);
                if (activeSubscriptions != null && !activeSubscriptions.isEmpty()) {
                    Set<String> planIds = activeSubscriptions.stream()
                            .map(UserSubscriptionEntity::getSubscriptionPlanId)
                            .collect(Collectors.toSet());
                    Set<String> planCourseIds = subscriptionPlanDomainService.getCourseIdsByPlanIds(planIds);
                    unlockedByPlan = planCourseIds.contains(courseId);
                }
            }

            boolean unlocked = owned || unlockedByPlan;
            dto.setUnlocked(unlocked);

            // 如果课程未解锁，提供可解锁的套餐列表
            if (!unlocked) {
                List<AppSubscriptionPlanDTO> unlockPlans = SubscriptionPlanAssembler.toAppDTOList(
                        subscriptionPlanDomainService.getPlansByCourseId(courseId)
                );
                dto.setUnlockPlans(unlockPlans);
            }

        } catch (Exception e) {
            // 容错：出现异常时默认未解锁
            dto.setUnlocked(false);
        }
    }
}
