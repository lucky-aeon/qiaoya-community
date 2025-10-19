package org.xhy.community.interfaces.interview.request;

import jakarta.validation.constraints.*;

import java.util.List;

public class CreateInterviewQuestionRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    @NotBlank
    private String answer;
    @NotNull
    @Min(1)
    @Max(5)
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

