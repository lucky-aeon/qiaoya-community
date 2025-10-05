package org.xhy.community.application.course.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.course.dto.CourseProgressDTO;
import org.xhy.community.application.course.dto.LearningRecordItemDTO;
import org.xhy.community.domain.course.entity.UserCourseProgressEntity;
// import removed: certificate replaced by tag system
import org.xhy.community.domain.course.service.CourseProgressDomainService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseProgressAppService {

    private final CourseProgressDomainService courseProgressDomainService;
    private final org.xhy.community.domain.course.service.CourseDomainService courseDomainService;
    private final org.xhy.community.domain.course.service.ChapterDomainService chapterDomainService;
    private final org.xhy.community.domain.tag.service.TagDomainService tagDomainService;

    public CourseProgressAppService(CourseProgressDomainService courseProgressDomainService,
                                    org.xhy.community.domain.course.service.CourseDomainService courseDomainService,
                                    org.xhy.community.domain.course.service.ChapterDomainService chapterDomainService,
                                    org.xhy.community.domain.tag.service.TagDomainService tagDomainService) {
        this.courseProgressDomainService = courseProgressDomainService;
        this.courseDomainService = courseDomainService;
        this.chapterDomainService = chapterDomainService;
        this.tagDomainService = tagDomainService;
    }

    public CourseProgressDTO reportProgress(String userId,
                                            org.xhy.community.interfaces.course.request.ReportChapterProgressRequest request) {
        // 组装值对象并更新章节进度
        var report = org.xhy.community.application.course.assembler.LearningProgressAssembler.fromReportRequest(userId, request);
        courseProgressDomainService.updateChapterProgress(report);

        // 发放标签改为事件驱动（监听课程完成事件），此处不直接发放

        return buildProgressDTO(userId, request.getCourseId());
    }

    public CourseProgressDTO getCourseProgress(String userId, String courseId) {
        return buildProgressDTO(userId, courseId);
    }

    private CourseProgressDTO buildProgressDTO(String userId, String courseId) {
        UserCourseProgressEntity agg = courseProgressDomainService.getAggregate(userId, courseId);
        // 用标签系统替代“证书”：课程完成标签是否已拥有
        boolean hasCompletionTag = tagDomainService.hasCourseCompletionTag(userId, courseId);
        
        CourseProgressDTO dto = new CourseProgressDTO();
        dto.setCourseId(courseId);
        if (agg != null) {
            dto.setTotalChapters(agg.getTotalChapters());
            dto.setCompletedChapters(agg.getCompletedChapters());
            dto.setProgressPercent(agg.getProgressPercent());
            dto.setLastAccessChapterId(agg.getLastAccessChapterId());
            dto.setLastAccessTime(agg.getLastAccessTime());
            dto.setCompleted(agg.getCompletedAt() != null);
            dto.setCompletedAt(agg.getCompletedAt());
        } else {
            dto.setTotalChapters(0);
            dto.setCompletedChapters(0);
            dto.setProgressPercent(0);
            dto.setCompleted(false);
        }
        dto.setHasCompletionTag(hasCompletionTag);
        return dto;
    }

    /**
     * 分页获取用户的学习记录（每门课程一条），包含最近观看章节与位置。
     */
    public IPage<LearningRecordItemDTO> listMyLearningRecords(String userId, int pageNum, int pageSize) {
        IPage<UserCourseProgressEntity> entityPage = courseProgressDomainService.listAggregatesByUser(userId, pageNum, pageSize);

        List<UserCourseProgressEntity> records = entityPage.getRecords();
        if (records.isEmpty()) {
            Page<LearningRecordItemDTO> empty = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
            empty.setRecords(Collections.emptyList());
            return empty;
        }

        Set<String> courseIds = records.stream().map(UserCourseProgressEntity::getCourseId).collect(Collectors.toSet());
        Set<String> chapterIds = records.stream()
                .map(UserCourseProgressEntity::getLastAccessChapterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, String> courseTitleMap = courseDomainService.getCourseTitleMapByIds(courseIds);
        Map<String, String> chapterTitleMap = chapterDomainService.getChapterTitleMapByIds(chapterIds);

        // 批量查询该用户在这些“最近章节”上的 positionSec
        Map<String, Integer> chapterPositionMap = courseProgressDomainService.getChapterPositionsForUser(userId, chapterIds);

        List<LearningRecordItemDTO> dtoList = records.stream().map(agg -> {
            LearningRecordItemDTO dto = new LearningRecordItemDTO();
            dto.setCourseId(agg.getCourseId());
            dto.setCourseTitle(courseTitleMap.get(agg.getCourseId()));
            dto.setTotalChapters(agg.getTotalChapters());
            dto.setCompletedChapters(agg.getCompletedChapters());
            dto.setProgressPercent(agg.getProgressPercent());
            dto.setCompleted(agg.getCompletedAt() != null);
            dto.setCompletedAt(agg.getCompletedAt());
            dto.setLastAccessChapterId(agg.getLastAccessChapterId());
            dto.setLastAccessTime(agg.getLastAccessTime());
            if (agg.getLastAccessChapterId() != null) {
                dto.setLastAccessChapterTitle(chapterTitleMap.get(agg.getLastAccessChapterId()));
                dto.setLastPositionSec(chapterPositionMap.getOrDefault(agg.getLastAccessChapterId(), 0));
            }
            return dto;
        }).toList();

        Page<LearningRecordItemDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
}
