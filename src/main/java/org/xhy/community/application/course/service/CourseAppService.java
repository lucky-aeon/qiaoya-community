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
    
    public CourseAppService(CourseDomainService courseDomainService,
                           ChapterDomainService chapterDomainService,
                           UserDomainService userDomainService,
                           SubscriptionDomainService subscriptionDomainService,
                           SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.courseDomainService = courseDomainService;
        this.chapterDomainService = chapterDomainService;
        this.userDomainService = userDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
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
        return PublicCourseAssembler.toPublicDetailDTO(course, chapters);
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
}
