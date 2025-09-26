package org.xhy.community.application.course.dto;

import org.xhy.community.domain.course.valueobject.CourseResource;
import org.xhy.community.domain.course.valueobject.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 前台课程列表DTO
 * 用于前台API返回课程概要信息
 */
public class FrontCourseDTO {
    
    private String id;
    private String title;
    private String description;
    private List<String> techStack;
    private String projectUrl;
    private List<String> tags;
    private BigDecimal rating;
    private CourseStatus status;
    private String authorName;
    private Integer totalReadingTime;
    private Integer chapterCount;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private String coverImage;
    private String demoUrl;
    private List<CourseResource> resources;
    private LocalDateTime createTime;
    private Boolean unlocked;
    
    public FrontCourseDTO() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
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
    
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    
    public Integer getTotalReadingTime() { return totalReadingTime; }
    public void setTotalReadingTime(Integer totalReadingTime) { this.totalReadingTime = totalReadingTime; }
    
    public Integer getChapterCount() { return chapterCount; }
    public void setChapterCount(Integer chapterCount) { this.chapterCount = chapterCount; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }

    public List<CourseResource> getResources() { return resources; }
    public void setResources(List<CourseResource> resources) { this.resources = resources; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Boolean getUnlocked() { return unlocked; }
    public void setUnlocked(Boolean unlocked) { this.unlocked = unlocked; }
}