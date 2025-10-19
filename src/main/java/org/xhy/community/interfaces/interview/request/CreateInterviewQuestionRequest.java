package org.xhy.community.interfaces.interview.request;

import jakarta.validation.constraints.*;

import java.util.List;

public class CreateInterviewQuestionRequest {
    @NotBlank
    private String title;
    private String description;
    private String answer;
    // 难度：对用户可选（默认3；由应用/领域层控制），管理员可在更新时修改
    private Integer rating;
    @NotBlank
    private String categoryId;
    private List<String> tags;  // 标签列表

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
