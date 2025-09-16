package org.xhy.community.domain.course.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.entity.BaseEntity;
import org.xhy.community.domain.course.valueobject.CourseStatus;
import org.xhy.community.infrastructure.converter.CourseStatusConverter;
import org.xhy.community.infrastructure.converter.StringListConverter;

import java.math.BigDecimal;
import java.util.List;

@TableName("courses")
public class CourseEntity extends BaseEntity {
    
    /** 课程标题 */
    private String title;
    
    /** 课程简介 */
    private String description;
    
    /** 技术栈，JSON格式存储 */
    @TableField(typeHandler = StringListConverter.class)
    private List<String> techStack;
    
    /** 项目地址 */
    private String projectUrl;
    
    /** 标签，JSON格式存储 */
    @TableField(typeHandler = StringListConverter.class)
    private List<String> tags;
    
    /** 课程评分，范围0.00-5.00 */
    private BigDecimal rating;
    
    /** 课程状态 */
    @TableField(typeHandler = CourseStatusConverter.class)
    private CourseStatus status;
    
    /** 作者用户ID */
    private String authorId;
    
    /** 总阅读时长（分钟） */
    private Integer totalReadingTime;
    
    public CourseEntity() {
    }
    
    public CourseEntity(String title, String description, String authorId) {
        this.title = title;
        this.description = description;
        this.authorId = authorId;
        this.status = CourseStatus.PENDING;
        this.rating = BigDecimal.ZERO;
        this.totalReadingTime = 0;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getTechStack() { return techStack; }
    public void setTechStack(List<String> techStack) { this.techStack = techStack; }
    
    public String getProjectUrl() { return projectUrl; }
    public void setProjectUrl(String projectUrl) { this.projectUrl = projectUrl; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }
    
    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }
    
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    
    public Integer getTotalReadingTime() { return totalReadingTime; }
    public void setTotalReadingTime(Integer totalReadingTime) { this.totalReadingTime = totalReadingTime; }
}