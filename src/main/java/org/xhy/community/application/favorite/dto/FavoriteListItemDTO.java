package org.xhy.community.application.favorite.dto;

import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.favorite.valueobject.FavoriteTargetType;

import java.time.LocalDateTime;

/**
 * 我的收藏列表项DTO（包含扩展信息）
 */
public class FavoriteListItemDTO {

    /** 收藏ID */
    private String id;

    /** 目标ID */
    private String targetId;

    /** 目标类型 */
    private FavoriteTargetType targetType;

    /** 目标标题 */
    private String title;

    /** 目标摘要/内容片段 */
    private String snippet;

    /** 作者ID */
    private String authorId;

    /** 作者名称 */
    private String authorName;

    /** 收藏时间 */
    private LocalDateTime createTime;

    /** 业务对象ID（仅 COMMENT 类型有值，表示评论所属的文章/课程等） */
    private String businessId;

    /** 业务对象类型（仅 COMMENT 类型有值，POST/COURSE/CHAPTER/INTERVIEW_QUESTION 等） */
    private BusinessType businessType;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public FavoriteTargetType getTargetType() { return targetType; }
    public void setTargetType(FavoriteTargetType targetType) { this.targetType = targetType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public BusinessType getBusinessType() { return businessType; }
    public void setBusinessType(BusinessType businessType) { this.businessType = businessType; }
}
