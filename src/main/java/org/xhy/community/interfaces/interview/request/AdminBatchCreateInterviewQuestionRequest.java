package org.xhy.community.interfaces.interview.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 管理员批量创建面试题请求
 * 入参仅包含标题与分类ID
 */
public class AdminBatchCreateInterviewQuestionRequest {

    @NotEmpty(message = "题目列表不能为空")
    @Valid
    private List<Item> items;

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        @NotBlank(message = "标题不能为空")
        private String title;
        @NotBlank(message = "分类ID不能为空")
        private String categoryId;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCategoryId() { return categoryId; }
        public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    }
}

