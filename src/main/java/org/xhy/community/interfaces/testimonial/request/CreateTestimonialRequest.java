package org.xhy.community.interfaces.testimonial.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTestimonialRequest {

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 2000, message = "评价内容长度不能超过2000个字符")
    private String content;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最少为1分")
    @Max(value = 5, message = "评分最多为5分")
    private Integer rating;

    public CreateTestimonialRequest() {
    }

    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
}