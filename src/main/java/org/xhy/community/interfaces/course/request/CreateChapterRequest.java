package org.xhy.community.interfaces.course.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateChapterRequest {
    
    @NotBlank(message = "章节标题不能为空")
    @Size(min = 2, max = 200, message = "章节标题长度必须在2-200字符之间")
    private String title;
    
    @NotBlank(message = "章节内容不能为空")
    @Size(min = 10, message = "章节内容长度不能少于10个字符")
    private String content;
    
    @NotBlank(message = "课程ID不能为空")
    private String courseId;
    
    @NotNull(message = "排序序号不能为空")
    private Integer sortOrder;
    
    private Integer readingTime;
    
    public CreateChapterRequest() {
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public Integer getReadingTime() { return readingTime; }
    public void setReadingTime(Integer readingTime) { this.readingTime = readingTime; }
}