package org.xhy.community.application.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.CourseAssembler;
import org.xhy.community.application.course.dto.FrontCourseDTO;
import org.xhy.community.application.course.dto.FrontCourseDetailDTO;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.course.query.CourseQuery;
import org.xhy.community.domain.course.valueobject.CourseStatus;
import org.xhy.community.interfaces.course.request.AppCourseQueryRequest;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CourseErrorCode;

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
    
    public CourseAppService(CourseDomainService courseDomainService,
                           ChapterDomainService chapterDomainService,
                           UserDomainService userDomainService) {
        this.courseDomainService = courseDomainService;
        this.chapterDomainService = chapterDomainService;
        this.userDomainService = userDomainService;
    }
    
    /**
     * 分页查询前台课程列表
     * 只返回已完成状态的课程
     *
     * @param request 查询请求参数
     * @return 课程列表分页结果
     */
    public IPage<FrontCourseDTO> queryAppCourses(AppCourseQueryRequest request) {
        // 转换查询参数，只查询已完成的课程
        CourseQuery query = CourseAssembler.fromAppQueryRequest(request);

        // 查询课程分页数据
        IPage<CourseEntity> coursePage = courseDomainService.getPublishedPagedCourses(query);
        
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
        
        // 批量查询作者名称
        Map<String, String> authorNameMap = userDomainService.getUserNameMapByIds(authorIds);
        
        // 提取课程ID集合
        Set<String> courseIds = coursePage.getRecords().stream()
                .map(CourseEntity::getId)
                .collect(Collectors.toSet());
        
        // 批量查询每个课程的章节数量
        Map<String, Integer> chapterCountMap = getChapterCountMap(courseIds);
        
        // 转换为前台DTO
        List<FrontCourseDTO> frontCourseDTOs = CourseAssembler.toFrontDTOList(
                coursePage.getRecords(), 
                authorNameMap, 
                chapterCountMap
        );
        
        // 构建结果分页对象
        IPage<FrontCourseDTO> result = coursePage.convert(entity -> (FrontCourseDTO) null);
        result.setRecords(frontCourseDTOs);
        return result;
    }
    
    /**
     * 获取前台课程详情
     * 包含课程信息和章节列表
     *
     * @param courseId 课程ID
     * @return 课程详情信息
     */
    public FrontCourseDetailDTO getAppCourseDetail(String courseId) {
        // 获取课程信息
        CourseEntity course = courseDomainService.getCourseById(courseId);

        // 获取作者信息
        String authorName = userDomainService.getUserById(course.getAuthorId()).getName();
        
        // 获取章节列表
        List<ChapterEntity> chapters = chapterDomainService.getChaptersByCourseId(courseId);
        
        // 转换为前台详情DTO
        return CourseAssembler.toFrontDetailDTO(course, authorName, chapters);
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