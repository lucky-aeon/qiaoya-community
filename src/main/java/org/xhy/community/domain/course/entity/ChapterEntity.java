package org.xhy.community.domain.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

import java.time.LocalDateTime;

@TableName("chapters")
public class ChapterEntity extends BaseEntity {
    
    /** 章节标题 */
    private String title;
    
    /** 章节内容 */
    private String content;
    
    /** 所属课程ID */
    private String courseId;
    
    /** 作者用户ID */
    private String authorId;
    
    /** 排序序号 */
    private Integer sortOrder;
    
    /** 预计阅读时长（分钟） */
    private Integer readingTime;

    /** 是否归档：归档章节仍可访问，但提示内容可能过时 */
    private Boolean archived;

    /** 归档原因 */
    private String archiveReason;

    /** 归档时间 */
    private LocalDateTime archivedAt;
    
    public ChapterEntity() {
    }
    
    public ChapterEntity(String title, String content, String courseId, String authorId, Integer sortOrder) {
        this.title = title;
        this.content = content;
        this.courseId = courseId;
        this.authorId = authorId;
        this.sortOrder = sortOrder;
        this.readingTime = 0;
        this.archived = false;
    }
    
    // Getters and Setters
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

    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }

    public String getArchiveReason() { return archiveReason; }
    public void setArchiveReason(String archiveReason) { this.archiveReason = archiveReason; }

    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }
}
