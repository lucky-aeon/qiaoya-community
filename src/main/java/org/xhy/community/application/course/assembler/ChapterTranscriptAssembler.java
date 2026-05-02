package org.xhy.community.application.course.assembler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.xhy.community.application.course.dto.AdminChapterTranscriptDTO;
import org.xhy.community.application.course.dto.ChapterTranscriptDTO;
import org.xhy.community.application.course.dto.ChapterTranscriptSegmentDTO;
import org.xhy.community.domain.course.entity.ChapterTranscriptEntity;
import org.xhy.community.domain.course.entity.ChapterTranscriptSegmentEntity;

import java.util.ArrayList;
import java.util.List;

public class ChapterTranscriptAssembler {

    private static final double QWEN_ASR_PRICE_PER_SECOND = 0.00022d;

    public static ChapterTranscriptDTO empty(String chapterId) {
        ChapterTranscriptDTO dto = new ChapterTranscriptDTO();
        dto.setChapterId(chapterId);
        dto.setStatus("NOT_GENERATED");
        return dto;
    }

    public static ChapterTranscriptDTO toDTO(ChapterTranscriptEntity entity,
                                             List<ChapterTranscriptSegmentEntity> segments,
                                             ObjectMapper objectMapper) {
        if (entity == null) {
            return null;
        }
        ChapterTranscriptDTO dto = new ChapterTranscriptDTO();
        dto.setChapterId(entity.getChapterId());
        dto.setResourceId(entity.getResourceId());
        dto.setStatus(entity.getStatus() == null ? null : entity.getStatus().name());
        dto.setDurationMs(entity.getDurationMs());
        dto.setText(entity.getText());
        dto.setSummary(entity.getSummary());
        dto.setCompletedAt(entity.getCompletedAt());
        dto.setKeyPoints(parseKeyPoints(entity.getKeyPointsJson(), objectMapper));
        dto.setSegments(toSegmentDTOs(segments));
        return dto;
    }

    public static AdminChapterTranscriptDTO emptyAdmin(String chapterId) {
        AdminChapterTranscriptDTO dto = new AdminChapterTranscriptDTO();
        dto.setChapterId(chapterId);
        dto.setStatus("NOT_GENERATED");
        return dto;
    }

    public static AdminChapterTranscriptDTO emptyAdmin(String chapterId, String errorCode, String errorMessage) {
        AdminChapterTranscriptDTO dto = emptyAdmin(chapterId);
        dto.setErrorCode(errorCode);
        dto.setErrorMessage(errorMessage);
        return dto;
    }

    public static AdminChapterTranscriptDTO toAdminDTO(ChapterTranscriptEntity entity) {
        if (entity == null) {
            return null;
        }
        AdminChapterTranscriptDTO dto = new AdminChapterTranscriptDTO();
        dto.setChapterId(entity.getChapterId());
        dto.setResourceId(entity.getResourceId());
        dto.setStatus(entity.getStatus() == null ? null : entity.getStatus().name());
        dto.setProvider(entity.getProvider() == null ? null : entity.getProvider().name());
        dto.setModel(entity.getModel());
        dto.setProviderTaskId(entity.getProviderTaskId());
        dto.setDurationMs(entity.getDurationMs());
        dto.setEstimatedCost(estimateCost(entity.getDurationMs()));
        dto.setErrorCode(entity.getErrorCode());
        dto.setErrorMessage(entity.getErrorMessage());
        dto.setSubmittedAt(entity.getSubmittedAt());
        dto.setCompletedAt(entity.getCompletedAt());
        return dto;
    }

    private static List<ChapterTranscriptSegmentDTO> toSegmentDTOs(List<ChapterTranscriptSegmentEntity> segments) {
        List<ChapterTranscriptSegmentDTO> list = new ArrayList<>();
        if (segments == null) {
            return list;
        }
        for (ChapterTranscriptSegmentEntity segment : segments) {
            ChapterTranscriptSegmentDTO dto = new ChapterTranscriptSegmentDTO();
            dto.setStartMs(segment.getStartMs());
            dto.setEndMs(segment.getEndMs());
            dto.setSpeaker(segment.getSpeaker());
            dto.setText(segment.getText());
            dto.setSortOrder(segment.getSortOrder());
            list.add(dto);
        }
        return list;
    }

    private static List<String> parseKeyPoints(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private static Double estimateCost(Long durationMs) {
        if (durationMs == null || durationMs <= 0) {
            return null;
        }
        return (durationMs / 1000.0d) * QWEN_ASR_PRICE_PER_SECOND;
    }
}
