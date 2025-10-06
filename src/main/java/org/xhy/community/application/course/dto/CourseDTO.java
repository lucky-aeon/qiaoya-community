package org.xhy.community.application.course.dto;

import org.xhy.community.domain.course.valueobject.CourseStatus;
import org.xhy.community.domain.course.valueobject.CourseResource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CourseDTO {
    
    private String id;
    private String title;
    private String description;
    private List<String> techStack;
    private String projectUrl;
    private List<String> tags;
    private BigDecimal rating;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String coverImage;
    private CourseStatus status;
    private String authorId;
    private Integer totalReadingTime;
    private String demoUrl;
    private List<CourseResource> resources;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer likeCount;
    private Integer sortOrder;
    
    public CourseDTO() {
    }
    
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
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    
    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }
    
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    
    public Integer getTotalReadingTime() { return totalReadingTime; }
    public void setTotalReadingTime(Integer totalReadingTime) { this.totalReadingTime = totalReadingTime; }

    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }

    public List<CourseResource> getResources() { return resources; }
    public void setResources(List<CourseResource> resources) { this.resources = resources; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
