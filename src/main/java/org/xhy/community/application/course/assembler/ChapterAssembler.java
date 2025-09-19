package org.xhy.community.application.course.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.course.dto.ChapterDTO;
import org.xhy.community.application.course.dto.FrontChapterDetailDTO;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.interfaces.course.request.CreateChapterRequest;
import org.xhy.community.interfaces.course.request.UpdateChapterRequest;

public class ChapterAssembler {

    public static ChapterDTO toDTO(ChapterEntity entity) {
        if (entity == null) {
            return null;
        }

        ChapterDTO dto = new ChapterDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public static FrontChapterDetailDTO toFrontDetailDTO(ChapterEntity entity, String courseName) {
        if (entity == null) {
            return null;
        }

        FrontChapterDetailDTO dto = new FrontChapterDetailDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setCourseName(courseName);
        return dto;
    }

    public static ChapterEntity fromCreateRequest(CreateChapterRequest request, String authorId) {
        if (request == null) {
            return null;
        }

        ChapterEntity entity = new ChapterEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setAuthorId(authorId);

        return entity;
    }

    public static ChapterEntity fromUpdateRequest(UpdateChapterRequest request, String chapterId) {
        if (request == null) {
            return null;
        }

        ChapterEntity entity = new ChapterEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(chapterId);

        return entity;
    }
}