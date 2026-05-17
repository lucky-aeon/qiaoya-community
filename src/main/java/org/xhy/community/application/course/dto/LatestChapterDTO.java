package org.xhy.community.application.course.dto;

import java.time.LocalDateTime;

public class LatestChapterDTO {

    private String id;
    private String title;
    private String courseId;
    private String courseName;
    private Integer sortOrder;
    private Integer readingTime;
    private LocalDateTime createTime;
    private Boolean archived;
    private String archiveReason;
    private LocalDateTime archivedAt;
    private Boolean courseArchived;
    private String courseArchiveReason;

    public LatestChapterDTO() {
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getReadingTime() { return readingTime; }
    public void setReadingTime(Integer readingTime) { this.readingTime = readingTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }

    public String getArchiveReason() { return archiveReason; }
    public void setArchiveReason(String archiveReason) { this.archiveReason = archiveReason; }

    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }

    public Boolean getCourseArchived() { return courseArchived; }
    public void setCourseArchived(Boolean courseArchived) { this.courseArchived = courseArchived; }

    public String getCourseArchiveReason() { return courseArchiveReason; }
    public void setCourseArchiveReason(String courseArchiveReason) { this.courseArchiveReason = courseArchiveReason; }
}
