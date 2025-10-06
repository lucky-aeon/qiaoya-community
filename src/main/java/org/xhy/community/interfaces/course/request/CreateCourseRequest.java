package org.xhy.community.interfaces.course.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;
import org.xhy.community.domain.course.valueobject.CourseResource;
import org.xhy.community.domain.course.valueobject.CourseStatus;
import java.math.BigDecimal;
import java.util.List;

public class CreateCourseRequest {
    
    @NotBlank(message = "课程标题不能为空")
    @Size(min = 2, max = 200, message = "课程标题长度必须在2-200字符之间")
    private String title;
    
    private String description;
    
    private List<String> techStack;
    
    private String projectUrl;
    
    private List<String> tags;
    
    private BigDecimal rating;
    
    @DecimalMin(value = "0.00", message = "课程售价不能为负数")
    private BigDecimal price;
    
    @DecimalMin(value = "0.00", message = "课程原价不能为负数")
    private BigDecimal originalPrice;
    
    private String coverImage;

    private String demoUrl;

    private List<CourseResource> resources;

    private CourseStatus status;

    // 排序序号（列表倒序显示，值越大越靠前）
    private Integer sortOrder;
    
    public CreateCourseRequest() {
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
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }

    public List<CourseResource> getResources() { return resources; }
    public void setResources(List<CourseResource> resources) { this.resources = resources; }

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
