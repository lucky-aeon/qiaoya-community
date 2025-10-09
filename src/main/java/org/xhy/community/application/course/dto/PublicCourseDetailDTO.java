package org.xhy.community.application.course.dto;

import org.xhy.community.application.subscription.dto.AppSubscriptionPlanDTO;
import org.xhy.community.domain.course.valueobject.CourseResource;
import org.xhy.community.domain.course.valueobject.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 公开课程详情DTO（不包含敏感链接字段）
 */
public class PublicCourseDetailDTO {

    private String id;
    private String title;
    private String description;
    private List<String> techStack;
    private List<String> tags;
    private BigDecimal rating;
    private CourseStatus status;
    private Integer totalReadingTime;
    private BigDecimal originalPrice;
    private BigDecimal price;
    private String demoUrl;
    private List<CourseResource> resources;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<FrontChapterDTO> chapters;
    private Integer likeCount;
    private List<AppSubscriptionPlanDTO> unlockPlans = new ArrayList<>();

    public PublicCourseDetailDTO() {}

    /**
     * 公开章节DTO
     */
    public static class FrontChapterDTO {
        private String id;
        private String title;
        private Integer sortOrder;
        private Integer readingTime;
        private LocalDateTime createTime;
        private Integer likeCount;

        public FrontChapterDTO() {}

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

        public Integer getReadingTime() { return readingTime; }
        public void setReadingTime(Integer readingTime) { this.readingTime = readingTime; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

        public Integer getLikeCount() { return likeCount; }
        public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
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

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }

    

    public Integer getTotalReadingTime() { return totalReadingTime; }
    public void setTotalReadingTime(Integer totalReadingTime) { this.totalReadingTime = totalReadingTime; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }

    public List<CourseResource> getResources() { return resources; }
    public void setResources(List<CourseResource> resources) { this.resources = resources; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public List<FrontChapterDTO> getChapters() { return chapters; }
    public void setChapters(List<FrontChapterDTO> chapters) { this.chapters = chapters; }

    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public List<AppSubscriptionPlanDTO> getUnlockPlans() { return unlockPlans; }
    public void setUnlockPlans(List<AppSubscriptionPlanDTO> unlockPlans) { this.unlockPlans = unlockPlans; }
}
