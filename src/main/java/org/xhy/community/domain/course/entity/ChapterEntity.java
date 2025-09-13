package org.xhy.community.domain.course.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.entity.BaseEntity;

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
    
    public ChapterEntity() {
    }
    
    public ChapterEntity(String title, String content, String courseId, String authorId, Integer sortOrder) {
        this.title = title;
        this.content = content;
        this.courseId = courseId;
        this.authorId = authorId;
        this.sortOrder = sortOrder;
        this.readingTime = 0;
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
}