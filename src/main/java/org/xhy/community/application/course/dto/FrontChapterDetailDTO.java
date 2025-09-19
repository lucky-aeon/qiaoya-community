package org.xhy.community.application.course.dto;

import java.time.LocalDateTime;

/**
 * 前台章节详情DTO
 * 用于前台展示章节详情，包含课程名称而非作者ID
 */
public class FrontChapterDetailDTO {

    private String id;
    private String title;
    private String content;
    private String courseId;
    private String courseName;
    private Integer sortOrder;
    private Integer readingTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public FrontChapterDetailDTO() {
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

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getReadingTime() { return readingTime; }
    public void setReadingTime(Integer readingTime) { this.readingTime = readingTime; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}