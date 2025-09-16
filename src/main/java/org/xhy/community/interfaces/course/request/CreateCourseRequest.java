package org.xhy.community.interfaces.course.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.xhy.community.domain.course.valueobject.CourseStatus;
import java.util.List;

public class CreateCourseRequest {
    
    @NotBlank(message = "课程标题不能为空")
    @Size(min = 2, max = 200, message = "课程标题长度必须在2-200字符之间")
    private String title;
    
    private String description;
    
    private List<String> techStack;
    
    private String projectUrl;
    
    private List<String> tags;
    
    private CourseStatus status;
    
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
    
    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }
}