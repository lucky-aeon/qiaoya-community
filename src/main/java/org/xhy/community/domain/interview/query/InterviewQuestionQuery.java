package org.xhy.community.domain.interview.query;

import org.xhy.community.domain.common.query.BasePageQuery;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.domain.interview.valueobject.ProblemStatus;

/**
 * 面试题查询对象（领域查询）
 * 统一封装分页与筛选条件，避免多参数传递
 */
public class InterviewQuestionQuery extends BasePageQuery {

    private String authorId;
    private String categoryId;
    private ProblemStatus status;
    private String title; // 标题关键字
    private String tag;     // 标签（单个匹配）
    private Integer minRating;
    private Integer maxRating;
    private AccessLevel accessLevel; // 用于USER鉴权隔离
    private Boolean publishedOnly;   // 公开题库视图（强制PUBLISHED）

    public InterviewQuestionQuery() {}

    public InterviewQuestionQuery(Integer pageNum, Integer pageSize) {
        super(pageNum, pageSize);
    }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public ProblemStatus getStatus() { return status; }
    public void setStatus(ProblemStatus status) { this.status = status; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public Integer getMinRating() { return minRating; }
    public void setMinRating(Integer minRating) { this.minRating = minRating; }
    public Integer getMaxRating() { return maxRating; }
    public void setMaxRating(Integer maxRating) { this.maxRating = maxRating; }
    public AccessLevel getAccessLevel() { return accessLevel; }
    public void setAccessLevel(AccessLevel accessLevel) { this.accessLevel = accessLevel; }
    public Boolean getPublishedOnly() { return publishedOnly; }
    public void setPublishedOnly(Boolean publishedOnly) { this.publishedOnly = publishedOnly; }
}
