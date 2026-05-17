package org.xhy.community.application.course.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.xhy.community.application.course.assembler.CourseAssembler;
import org.xhy.community.application.course.dto.CourseDTO;
import org.xhy.community.application.transcript.service.ChapterTranscriptAppService;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.query.CourseQuery;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.like.service.LikeDomainService;
import org.xhy.community.domain.like.valueobject.LikeTargetType;
import org.xhy.community.application.like.helper.LikeCountHelper;
import org.xhy.community.interfaces.course.request.CreateCourseRequest;
import org.xhy.community.interfaces.course.request.UpdateCourseRequest;
import org.xhy.community.interfaces.course.request.CourseQueryRequest;

@Service
public class AdminCourseAppService {
    
    private final CourseDomainService courseDomainService;
    private final ChapterDomainService chapterDomainService;
    private final LikeDomainService likeDomainService;
    private final ChapterTranscriptAppService chapterTranscriptAppService;
    
    public AdminCourseAppService(CourseDomainService courseDomainService,
                                 ChapterDomainService chapterDomainService,
                                 LikeDomainService likeDomainService,
                                 ChapterTranscriptAppService chapterTranscriptAppService) {
        this.courseDomainService = courseDomainService;
        this.chapterDomainService = chapterDomainService;
        this.likeDomainService = likeDomainService;
        this.chapterTranscriptAppService = chapterTranscriptAppService;
    }
    
    public CourseDTO createCourse(CreateCourseRequest request, String authorId) {
        CourseEntity course = CourseAssembler.fromCreateRequest(request, authorId);
        
        CourseEntity createdCourse = courseDomainService.createCourse(course);
        
        return CourseAssembler.toDTO(createdCourse);
    }
    
    public CourseDTO updateCourse(String courseId, UpdateCourseRequest request) {
        CourseEntity course = CourseAssembler.fromUpdateRequest(request, courseId);
        
        CourseEntity updatedCourse = courseDomainService.updateCourse(course);
        
        return CourseAssembler.toDTO(updatedCourse);
    }

    public void deleteCourse(String courseId) {
        courseDomainService.deleteCourse(courseId);
    }

    @Transactional
    public CourseDTO archiveCourse(String courseId, String reason) {
        CourseEntity course = courseDomainService.archiveCourse(courseId, reason);
        chapterDomainService.archiveChaptersByCourseId(courseId, reason);
        return CourseAssembler.toDTO(course);
    }

    @Transactional
    public CourseDTO unarchiveCourse(String courseId) {
        CourseEntity course = courseDomainService.unarchiveCourse(courseId);
        chapterDomainService.unarchiveChaptersByCourseId(courseId);
        return CourseAssembler.toDTO(course);
    }
    
    public CourseDTO getCourseById(String courseId) {
        CourseEntity course = courseDomainService.getCourseById(courseId);
        CourseDTO dto = CourseAssembler.toDTO(course);
        dto.setLikeCount(LikeCountHelper.getLikeCount(courseId, LikeTargetType.COURSE, likeDomainService));
        return dto;
    }
    
    public IPage<CourseDTO> getPagedCourses(CourseQueryRequest request) {
        CourseQuery query = CourseAssembler.fromAdminPageRequest(request.getPageNum(), request.getPageSize(), request.getArchived());
        IPage<CourseEntity> coursePage = courseDomainService.getPagedCourses(query);

        IPage<CourseDTO> dtoPage = coursePage.convert(CourseAssembler::toDTO);
        if (dtoPage.getRecords() != null && !dtoPage.getRecords().isEmpty()) {
            LikeCountHelper.fillLikeCount(dtoPage.getRecords(), CourseDTO::getId, LikeTargetType.COURSE, CourseDTO::setLikeCount, likeDomainService);
        }
        return dtoPage;
    }

    public int batchGenerateCourseTranscripts(String courseId) {
        return chapterTranscriptAppService.batchGenerateByCourse(courseId);
    }
}
