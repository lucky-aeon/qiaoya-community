package org.xhy.community.interfaces.post.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreatePostRequest {
    
    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 200, message = "标题长度必须在5-200字符之间")
    private String title;
    
    @NotBlank(message = "内容不能为空")
    @Size(min = 10, message = "内容长度不能少于10个字符")
    private String content;
    
    @Size(max = 500, message = "概要长度不能超过500个字符")
    private String summary;
    
    @Size(max = 500, message = "封面图片URL长度不能超过500个字符")
    private String coverImage;
    
    @NotNull(message = "分类ID不能为空")
    private String categoryId;
    
    public CreatePostRequest() {
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
}