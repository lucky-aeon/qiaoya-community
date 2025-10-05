package org.xhy.community.application.post.dto;

import org.xhy.community.domain.post.valueobject.CategoryType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公开文章详情DTO
 * 用于公开API返回文章详细信息，包含完整内容
 */
public class FrontPostDetailDTO {
    
    private String id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private String authorName;
    private String authorAvatar;
    private String authorDescription;
    private String authorId;
    private String categoryId;
    private String categoryName;
    private CategoryType categoryType;
    private Integer likeCount;
    private Integer viewCount;
    private Integer commentCount;
    private Boolean isTop;
    private LocalDateTime publishTime;
    private List<String> tags;
    // 问答帖子：前端用于渲染采纳标记
    private java.util.List<String> acceptedCommentIds;
    
    public FrontPostDetailDTO() {}
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }

    public String getAuthorDescription() { return authorDescription; }
    public void setAuthorDescription(String authorDescription) { this.authorDescription = authorDescription; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public CategoryType getCategoryType() { return categoryType; }
    public void setCategoryType(CategoryType categoryType) { this.categoryType = categoryType; }
    
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    
    public Integer getCommentCount() { return commentCount; }
    public void setCommentCount(Integer commentCount) { this.commentCount = commentCount; }
    
    public Boolean getIsTop() { return isTop; }
    public void setIsTop(Boolean isTop) { this.isTop = isTop; }
    
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getTop() {
        return isTop;
    }

    public void setTop(Boolean top) {
        isTop = top;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public java.util.List<String> getAcceptedCommentIds() {
        return acceptedCommentIds;
    }

    public void setAcceptedCommentIds(java.util.List<String> acceptedCommentIds) {
        this.acceptedCommentIds = acceptedCommentIds;
    }
}
