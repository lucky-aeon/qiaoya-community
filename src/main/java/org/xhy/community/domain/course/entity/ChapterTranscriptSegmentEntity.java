package org.xhy.community.domain.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

@TableName("chapter_transcript_segments")
public class ChapterTranscriptSegmentEntity extends BaseEntity {

    private String transcriptId;
    private String courseId;
    private String chapterId;
    private Long startMs;
    private Long endMs;
    private String speaker;
    private String text;
    private Integer sortOrder;

    public static ChapterTranscriptSegmentEntity of(String transcriptId, String courseId, String chapterId,
                                                    Long startMs, Long endMs, String speaker,
                                                    String text, Integer sortOrder) {
        ChapterTranscriptSegmentEntity entity = new ChapterTranscriptSegmentEntity();
        entity.setTranscriptId(transcriptId);
        entity.setCourseId(courseId);
        entity.setChapterId(chapterId);
        entity.setStartMs(startMs);
        entity.setEndMs(endMs);
        entity.setSpeaker(speaker);
        entity.setText(text);
        entity.setSortOrder(sortOrder);
        return entity;
    }

    public String getTranscriptId() { return transcriptId; }
    public void setTranscriptId(String transcriptId) { this.transcriptId = transcriptId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getChapterId() { return chapterId; }
    public void setChapterId(String chapterId) { this.chapterId = chapterId; }

    public Long getStartMs() { return startMs; }
    public void setStartMs(Long startMs) { this.startMs = startMs; }

    public Long getEndMs() { return endMs; }
    public void setEndMs(Long endMs) { this.endMs = endMs; }

    public String getSpeaker() { return speaker; }
    public void setSpeaker(String speaker) { this.speaker = speaker; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
