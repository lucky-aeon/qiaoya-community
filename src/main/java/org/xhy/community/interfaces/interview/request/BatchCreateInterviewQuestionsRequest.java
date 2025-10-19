package org.xhy.community.interfaces.interview.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 用户批量创建面试题请求
 * 入参：多个标题 + 单一分类ID
 */
public class BatchCreateInterviewQuestionsRequest {

    @NotBlank(message = "分类ID不能为空")
    private String categoryId;

    @NotEmpty(message = "标题列表不能为空")
    @Valid
    private List<@NotBlank(message = "标题不能为空") String> titles;

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public List<String> getTitles() { return titles; }
    public void setTitles(List<String> titles) { this.titles = titles; }
}

