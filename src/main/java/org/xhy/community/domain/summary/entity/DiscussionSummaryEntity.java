package org.xhy.community.domain.summary.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.summary.valueobject.SummaryTargetType;
import org.xhy.community.infrastructure.converter.SummaryTargetTypeConverter;

/**
 * 通用讨论摘要（文章、章节等）。
 * 仅保存目标标识与摘要文本。
 */
@TableName("discussion_summaries")
public class DiscussionSummaryEntity extends BaseEntity {

    /** 目标类型：文章/章节 */
    @TableField(typeHandler = SummaryTargetTypeConverter.class)
    private SummaryTargetType targetType;

    /** 目标ID */
    private String targetId;

    /** 摘要内容（中文要点） */
    private String summary;

    public SummaryTargetType getTargetType() { return targetType; }
    public void setTargetType(SummaryTargetType targetType) { this.targetType = targetType; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
}

