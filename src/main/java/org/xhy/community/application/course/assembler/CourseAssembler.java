package org.xhy.community.application.course.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.course.dto.CourseDTO;
import org.xhy.community.application.course.dto.SimpleCourseDTO;
import org.xhy.community.application.course.dto.FrontCourseDTO;
import org.xhy.community.application.course.dto.FrontCourseDetailDTO;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.query.CourseQuery;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.interfaces.course.request.CreateCourseRequest;
import org.xhy.community.interfaces.course.request.UpdateCourseRequest;
import org.xhy.community.interfaces.course.request.AppCourseQueryRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CourseAssembler {
    
    public static CourseQuery fromPageRequest(Integer pageNum, Integer pageSize) {
        return new CourseQuery(pageNum, pageSize);
    }
    
    public static CourseDTO toDTO(CourseEntity entity) {
        if (entity == null) {
            return null;
        }
        
        CourseDTO dto = new CourseDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    public static CourseEntity fromCreateRequest(CreateCourseRequest request, String authorId) {
        if (request == null) {
            return null;
        }

        CourseEntity entity = new CourseEntity();

        BeanUtils.copyProperties(request, entity);
        entity.setAuthorId(authorId);

        return entity;
    }
    
    public static CourseEntity fromUpdateRequest(UpdateCourseRequest request, String courseId) {
        if (request == null) {
            return null;
        }
        
        CourseEntity entity = new CourseEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(courseId);
        
        return entity;
    }
    
    public static SimpleCourseDTO toSimpleDTO(CourseEntity entity) {
        if (entity == null) {
            return null;
        }
        
        SimpleCourseDTO dto = new SimpleCourseDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    /**
     * 将CourseEntity转换为前台课程列表DTO（使用UserEntity）
     */
    public static FrontCourseDTO toFrontDTO(CourseEntity entity, UserEntity author, Integer chapterCount, Integer totalReadingTime) {
        if (entity == null) {
            return null;
        }

        FrontCourseDTO dto = new FrontCourseDTO();
        BeanUtils.copyProperties(entity, dto);
        if (author != null) {
            dto.setAuthorName(author.getName());
        }
        dto.setChapterCount(chapterCount);
        dto.setTotalReadingTime(totalReadingTime);
        return dto;
    }

    /**
     * 批量转换为前台课程列表DTO（使用UserEntity）
     */
    public static List<FrontCourseDTO> toFrontDTOList(List<CourseEntity> entities,
                                                      Map<String, UserEntity> authorMap,
                                                      Map<String, Integer> chapterCountMap,
                                                      Map<String, Integer> totalReadingTimeMap) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(entity -> toFrontDTO(entity,
                                        authorMap.get(entity.getAuthorId()),
                                        chapterCountMap.getOrDefault(entity.getId(), 0),
                                        totalReadingTimeMap.getOrDefault(entity.getId(), 0)))
                .collect(Collectors.toList());
    }

    /**
     * 将CourseEntity转换为前台课程详情DTO（使用UserEntity）
     */
    public static FrontCourseDetailDTO toFrontDetailDTO(CourseEntity entity, UserEntity author, List<ChapterEntity> chapters) {
        if (entity == null) {
            return null;
        }

        FrontCourseDetailDTO dto = new FrontCourseDetailDTO();
        BeanUtils.copyProperties(entity, dto);
        if (author != null) {
            dto.setAuthorName(author.getName());
        }

        // 转换章节信息
        if (chapters != null && !chapters.isEmpty()) {
            List<FrontCourseDetailDTO.FrontChapterDTO> chapterDTOs = chapters.stream()
                    .map(CourseAssembler::toFrontChapterDTO)
                    .collect(Collectors.toList());
            dto.setChapters(chapterDTOs);

            // 聚合总阅读时长
            int totalReadingTime = chapters.stream()
                    .map(ChapterEntity::getReadingTime)
                    .filter(java.util.Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();
            dto.setTotalReadingTime(totalReadingTime);
        }

        return dto;
    }

    /**
     * 将ChapterEntity转换为前台章节DTO
     */
    public static FrontCourseDetailDTO.FrontChapterDTO toFrontChapterDTO(ChapterEntity entity) {
        if (entity == null) {
            return null;
        }

        FrontCourseDetailDTO.FrontChapterDTO dto = new FrontCourseDetailDTO.FrontChapterDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 将前台查询请求转换为CourseQuery
     */
    public static CourseQuery fromAppQueryRequest(AppCourseQueryRequest request) {
        if (request == null) {
            return new CourseQuery(1, 10);
        }

        return new CourseQuery(request.getPageNum(), request.getPageSize());
    }
}
