package org.xhy.community.interfaces.post.request;

import jakarta.validation.constraints.Size;
import java.util.List;

public class UpdatePostRequest {
    
    @Size(min = 5, max = 200, message = "标题长度必须在5-200字符之间")
    private String title;
    
    @Size(min = 10, message = "内容长度不能少于10个字符")
    private String content;
    
    @Size(max = 500, message = "概要长度不能超过500个字符")
    private String summary;
    
    @Size(max = 500, message = "封面图片URL长度不能超过500个字符")
    private String coverImage;
    
    private String categoryId;

    // 标签（可选）
    private List<String> tags;
    
    public UpdatePostRequest() {
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

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
