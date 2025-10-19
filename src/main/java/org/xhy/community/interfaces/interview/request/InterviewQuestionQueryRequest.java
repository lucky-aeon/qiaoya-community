package org.xhy.community.interfaces.interview.request;

import org.xhy.community.domain.interview.valueobject.ProblemStatus;
import org.xhy.community.interfaces.common.request.PageRequest;

public class InterviewQuestionQueryRequest extends PageRequest {

    private ProblemStatus status;
    private String categoryId;
    private String title;  // 标题搜索关键词
    private String tag;  // 标签筛选（单个标签）
    private Integer minRating;  // 最小难度
    private Integer maxRating;  // 最大难度

    public InterviewQuestionQueryRequest() {
    }

    public ProblemStatus getStatus() {
        return status;
    }

    public void setStatus(ProblemStatus status) {
        this.status = status;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Integer getMinRating() {
        return minRating;
    }

    public void setMinRating(Integer minRating) {
        this.minRating = minRating;
    }

    public Integer getMaxRating() {
        return maxRating;
    }

    public void setMaxRating(Integer maxRating) {
        this.maxRating = maxRating;
    }
}
