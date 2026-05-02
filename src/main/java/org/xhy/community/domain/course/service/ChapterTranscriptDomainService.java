package org.xhy.community.domain.course.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.domain.course.entity.ChapterTranscriptEntity;
import org.xhy.community.domain.course.entity.ChapterTranscriptSegmentEntity;
import org.xhy.community.domain.course.repository.ChapterTranscriptRepository;
import org.xhy.community.domain.course.repository.ChapterTranscriptSegmentRepository;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptProvider;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
public class ChapterTranscriptDomainService {

    private final ChapterTranscriptRepository transcriptRepository;
    private final ChapterTranscriptSegmentRepository segmentRepository;

    public ChapterTranscriptDomainService(ChapterTranscriptRepository transcriptRepository,
                                          ChapterTranscriptSegmentRepository segmentRepository) {
        this.transcriptRepository = transcriptRepository;
        this.segmentRepository = segmentRepository;
    }

    @Transactional
    public ChapterTranscriptEntity createPendingIfAbsent(String courseId, String chapterId, String resourceId,
                                                         ChapterTranscriptProvider provider, String model,
                                                         String language) {
        ChapterTranscriptEntity existing = getActiveByChapterAndResource(chapterId, resourceId);
        if (existing != null) {
            return existing;
        }
        ChapterTranscriptEntity entity = ChapterTranscriptEntity.pending(courseId, chapterId, resourceId, provider, model, language);
        transcriptRepository.insert(entity);
        return entity;
    }

    public ChapterTranscriptEntity getActiveByChapterId(String chapterId) {
        return transcriptRepository.selectOne(new LambdaQueryWrapper<ChapterTranscriptEntity>()
                .eq(ChapterTranscriptEntity::getChapterId, chapterId)
                .orderByDesc(ChapterTranscriptEntity::getCreateTime)
                .last("LIMIT 1"));
    }

    public ChapterTranscriptEntity getActiveByChapterAndResource(String chapterId, String resourceId) {
        return transcriptRepository.selectOne(new LambdaQueryWrapper<ChapterTranscriptEntity>()
                .eq(ChapterTranscriptEntity::getChapterId, chapterId)
                .eq(ChapterTranscriptEntity::getResourceId, resourceId)
                .last("LIMIT 1"));
    }

    public List<ChapterTranscriptSegmentEntity> getSegments(String transcriptId) {
        return segmentRepository.selectList(new LambdaQueryWrapper<ChapterTranscriptSegmentEntity>()
                .eq(ChapterTranscriptSegmentEntity::getTranscriptId, transcriptId)
                .orderByAsc(ChapterTranscriptSegmentEntity::getSortOrder));
    }

    public List<ChapterTranscriptEntity> listPendingForSubmit(int limit) {
        return transcriptRepository.selectList(new LambdaQueryWrapper<ChapterTranscriptEntity>()
                .eq(ChapterTranscriptEntity::getStatus, ChapterTranscriptStatus.PENDING)
                .orderByAsc(ChapterTranscriptEntity::getCreateTime)
                .last("LIMIT " + Math.max(1, limit)));
    }

    public List<ChapterTranscriptEntity> listSubmittedOrRunningForPoll(int limit) {
        return transcriptRepository.selectList(new LambdaQueryWrapper<ChapterTranscriptEntity>()
                .in(ChapterTranscriptEntity::getStatus, List.of(
                        ChapterTranscriptStatus.SUBMITTED,
                        ChapterTranscriptStatus.RUNNING
                ))
                .orderByAsc(ChapterTranscriptEntity::getUpdateTime)
                .last("LIMIT " + Math.max(1, limit)));
    }

    public void markSubmitted(String id, String providerTaskId) {
        transcriptRepository.update(null, new LambdaUpdateWrapper<ChapterTranscriptEntity>()
                .eq(ChapterTranscriptEntity::getId, id)
                .set(ChapterTranscriptEntity::getProviderTaskId, providerTaskId)
                .set(ChapterTranscriptEntity::getStatus, ChapterTranscriptStatus.SUBMITTED)
                .set(ChapterTranscriptEntity::getSubmittedAt, LocalDateTime.now())
                .set(ChapterTranscriptEntity::getErrorCode, null)
                .set(ChapterTranscriptEntity::getErrorMessage, null));
    }

    public void markRunning(String id) {
        ChapterTranscriptEntity update = new ChapterTranscriptEntity();
        update.setId(id);
        update.setStatus(ChapterTranscriptStatus.RUNNING);
        transcriptRepository.updateById(update);
    }

    @Transactional
    public void markSucceeded(String id, String text, String summary, String keyPointsJson,
                              String rawResultJson, Long durationMs,
                              Collection<ChapterTranscriptSegmentEntity> segments) {
        ChapterTranscriptEntity current = transcriptRepository.selectById(id);
        if (current == null) {
            return;
        }

        segmentRepository.delete(new LambdaQueryWrapper<ChapterTranscriptSegmentEntity>()
                .eq(ChapterTranscriptSegmentEntity::getTranscriptId, id));

        if (segments != null) {
            for (ChapterTranscriptSegmentEntity segment : segments) {
                segment.setTranscriptId(id);
                segment.setCourseId(current.getCourseId());
                segment.setChapterId(current.getChapterId());
                segmentRepository.insert(segment);
            }
        }

        transcriptRepository.update(null, new LambdaUpdateWrapper<ChapterTranscriptEntity>()
                .eq(ChapterTranscriptEntity::getId, id)
                .set(ChapterTranscriptEntity::getStatus, ChapterTranscriptStatus.SUCCEEDED)
                .set(ChapterTranscriptEntity::getText, text)
                .set(ChapterTranscriptEntity::getSummary, summary)
                .set(ChapterTranscriptEntity::getKeyPointsJson, keyPointsJson)
                .set(ChapterTranscriptEntity::getRawResultJson, rawResultJson)
                .set(ChapterTranscriptEntity::getDurationMs, durationMs)
                .set(ChapterTranscriptEntity::getCompletedAt, LocalDateTime.now())
                .set(ChapterTranscriptEntity::getErrorCode, null)
                .set(ChapterTranscriptEntity::getErrorMessage, null));
    }

    public void markFailed(String id, String errorCode, String errorMessage) {
        ChapterTranscriptEntity update = new ChapterTranscriptEntity();
        update.setId(id);
        update.setStatus(ChapterTranscriptStatus.FAILED);
        update.setErrorCode(errorCode);
        update.setErrorMessage(truncate(errorMessage, 2000));
        transcriptRepository.updateById(update);
    }

    public void resetForRetry(String id) {
        transcriptRepository.update(null, new LambdaUpdateWrapper<ChapterTranscriptEntity>()
                .eq(ChapterTranscriptEntity::getId, id)
                .set(ChapterTranscriptEntity::getStatus, ChapterTranscriptStatus.PENDING)
                .set(ChapterTranscriptEntity::getProviderTaskId, null)
                .set(ChapterTranscriptEntity::getSubmittedAt, null)
                .set(ChapterTranscriptEntity::getCompletedAt, null)
                .set(ChapterTranscriptEntity::getErrorCode, null)
                .set(ChapterTranscriptEntity::getErrorMessage, null));
    }

    @Transactional
    public void softDeleteActiveForChapter(String chapterId) {
        List<ChapterTranscriptEntity> list = transcriptRepository.selectList(new LambdaQueryWrapper<ChapterTranscriptEntity>()
                .eq(ChapterTranscriptEntity::getChapterId, chapterId));
        for (ChapterTranscriptEntity transcript : list) {
            segmentRepository.delete(new LambdaQueryWrapper<ChapterTranscriptSegmentEntity>()
                    .eq(ChapterTranscriptSegmentEntity::getTranscriptId, transcript.getId()));
        }
        transcriptRepository.delete(new LambdaQueryWrapper<ChapterTranscriptEntity>()
                .eq(ChapterTranscriptEntity::getChapterId, chapterId));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
