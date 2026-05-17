package org.xhy.community.application.course.dto;

import org.xhy.community.application.transcript.dto.AdminChapterTranscriptDTO;

import java.time.LocalDateTime;

public class ChapterDTO {
    
    private String id;
    private String title;
    private String content;
    private String courseId;
    private String authorId;
    private Integer sortOrder;
    private Integer readingTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer likeCount;
    private AdminChapterTranscriptDTO transcript;
    private Boolean archived;
    private String archiveReason;
    private LocalDateTime archivedAt;
    
    public ChapterDTO() {
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public Integer getReadingTime() { return readingTime; }
    public void setReadingTime(Integer readingTime) { this.readingTime = readingTime; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public AdminChapterTranscriptDTO getTranscript() { return transcript; }
    public void setTranscript(AdminChapterTranscriptDTO transcript) { this.transcript = transcript; }

    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }

    public String getArchiveReason() { return archiveReason; }
    public void setArchiveReason(String archiveReason) { this.archiveReason = archiveReason; }

    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
}
