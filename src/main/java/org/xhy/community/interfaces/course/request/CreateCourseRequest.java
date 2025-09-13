package org.xhy.community.interfaces.course.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCourseRequest {
    
    @NotBlank(message = "课程标题不能为空")
    @Size(min = 5, max = 200, message = "课程标题长度必须在5-200字符之间")
    private String title;
    
    @Size(max = 2000, message = "课程简介长度不能超过2000个字符")
    private String description;
    
    @Size(max = 1000, message = "技术栈长度不能超过1000个字符")
    private String techStack;
    
    @Size(max = 500, message = "项目地址长度不能超过500个字符")
    private String projectUrl;
    
    @Size(max = 500, message = "标签长度不能超过500个字符")
    private String tags;
    
    public CreateCourseRequest() {
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }
    
    public String getProjectUrl() { return projectUrl; }
    public void setProjectUrl(String projectUrl) { this.projectUrl = projectUrl; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
}