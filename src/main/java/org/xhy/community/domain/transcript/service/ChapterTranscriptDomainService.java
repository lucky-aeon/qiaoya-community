package org.xhy.community.domain.transcript.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.domain.transcript.entity.ChapterTranscriptEntity;
import org.xhy.community.domain.transcript.entity.ChapterTranscriptSegmentEntity;
import org.xhy.community.domain.transcript.repository.ChapterTranscriptRepository;
import org.xhy.community.domain.transcript.repository.ChapterTranscriptSegmentRepository;

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

    public List<ChapterTranscriptEntity> listByStatuses(Collection<String> statuses, int limit) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }
        return transcriptRepository.selectList(new LambdaQueryWrapper<ChapterTranscriptEntity>()
                .in(ChapterTranscriptEntity::getStatus, statuses)
                .orderByAsc(ChapterTranscriptEntity::getCreateTime)
                .last("LIMIT " + Math.max(1, limit)));
    }

    public List<ChapterTranscriptSegmentEntity> listSegments(String transcriptId) {
        return segmentRepository.selectList(new LambdaQueryWrapper<ChapterTranscriptSegmentEntity>()
                .eq(ChapterTranscriptSegmentEntity::getTranscriptId, transcriptId)
                .orderByAsc(ChapterTranscriptSegmentEntity::getSortOrder));
    }

    public ChapterTranscriptEntity create(ChapterTranscriptEntity transcript) {
        transcriptRepository.insert(transcript);
        return transcript;
    }

    public void update(ChapterTranscriptEntity transcript) {
        transcriptRepository.updateById(transcript);
    }

    @Transactional
    public void replaceSegments(String transcriptId, List<ChapterTranscriptSegmentEntity> segments) {
        segmentRepository.delete(new LambdaQueryWrapper<ChapterTranscriptSegmentEntity>()
                .eq(ChapterTranscriptSegmentEntity::getTranscriptId, transcriptId));
        if (segments == null || segments.isEmpty()) {
            return;
        }
        for (ChapterTranscriptSegmentEntity segment : segments) {
            segmentRepository.insert(segment);
        }
    }

    @Transactional
    public void softDeleteTranscriptAndSegments(String transcriptId) {
        transcriptRepository.deleteById(transcriptId);
        segmentRepository.delete(new LambdaQueryWrapper<ChapterTranscriptSegmentEntity>()
                .eq(ChapterTranscriptSegmentEntity::getTranscriptId, transcriptId));
    }
}
