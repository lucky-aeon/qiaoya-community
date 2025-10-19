package org.xhy.community.domain.interview.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.interview.valueobject.ProblemStatus;
import org.xhy.community.infrastructure.converter.ProblemStatusConverter;
import org.xhy.community.infrastructure.converter.MapJsonTypeHandler;
import org.xhy.community.infrastructure.converter.UniversalListConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@TableName("interview_questions")
public class InterviewQuestionEntity extends BaseEntity {

    /** 题目名称 */
    private String title;

    /** 题目描述/问题本身 */
    private String description;

    /** 参考答案/要点 */
    private String answer;

    /** 评分（1-5） */
    private Integer rating;

    /** 分类ID（要求类别类型为 INTERVIEW） */
    private String categoryId;

    /** 作者ID */
    private String authorId;

    /** 发布状态 */
    @TableField(typeHandler = ProblemStatusConverter.class)
    private ProblemStatus status;

    /** 发布时间（首次发布时写入） */
    private LocalDateTime publishTime;

    /** 标签（JSON格式存储，如: ["Vue3","响应式原理","源码"]） */
    @TableField(typeHandler = UniversalListConverter.class)
    private List<String> tags;

    /** 额外扩展信息（低频字段） */
    @TableField(value = "extra", typeHandler = MapJsonTypeHandler.class)
    private Map<String, Object> extra;

    public InterviewQuestionEntity() {}

    public InterviewQuestionEntity(String title, String description, String answer, Integer rating,
                                   String categoryId, String authorId) {
        this.title = title;
        this.description = description;
        this.answer = answer;
        this.rating = rating;
        this.categoryId = categoryId;
        this.authorId = authorId;
        this.status = ProblemStatus.DRAFT;
    }

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

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public ProblemStatus getStatus() { return status; }
    public void setStatus(ProblemStatus status) { this.status = status; }

    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Map<String, Object> getExtra() { return extra; }
    public void setExtra(Map<String, Object> extra) { this.extra = extra; }
}
