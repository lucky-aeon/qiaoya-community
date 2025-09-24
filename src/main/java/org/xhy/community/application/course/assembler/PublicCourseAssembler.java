package org.xhy.community.application.course.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.course.dto.PublicCourseDTO;
import org.xhy.community.application.course.dto.PublicCourseDetailDTO;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PublicCourseAssembler {

    public static PublicCourseDTO toPublicDTO(CourseEntity entity, Integer chapterCount) {
        if (entity == null) {
            return null;
        }
        PublicCourseDTO dto = new PublicCourseDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setChapterCount(chapterCount);
        return dto;
    }

    public static List<PublicCourseDTO> toPublicDTOList(List<CourseEntity> entities,
                                                        Map<String, Integer> chapterCountMap) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }
        return entities.stream()
                .map(entity -> toPublicDTO(entity,
                        chapterCountMap.getOrDefault(entity.getId(), 0)))
                .collect(Collectors.toList());
    }

    public static PublicCourseDetailDTO toPublicDetailDTO(CourseEntity entity,
                                                          List<ChapterEntity> chapters) {
        if (entity == null) {
            return null;
        }
        PublicCourseDetailDTO dto = new PublicCourseDetailDTO();
        BeanUtils.copyProperties(entity, dto);
        if (chapters != null && !chapters.isEmpty()) {
            List<PublicCourseDetailDTO.FrontChapterDTO> chapterDTOs = chapters.stream()
                    .map(PublicCourseAssembler::toPublicChapterDTO)
                    .collect(Collectors.toList());
            dto.setChapters(chapterDTOs);
        }
        return dto;
    }

    public static PublicCourseDetailDTO.FrontChapterDTO toPublicChapterDTO(ChapterEntity entity) {
        if (entity == null) {
            return null;
        }
        PublicCourseDetailDTO.FrontChapterDTO dto = new PublicCourseDetailDTO.FrontChapterDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
