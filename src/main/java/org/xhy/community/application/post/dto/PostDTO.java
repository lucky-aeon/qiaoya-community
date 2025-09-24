package org.xhy.community.application.post.dto;

import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.domain.post.valueobject.QAResolveStatus;

import java.time.LocalDateTime;
import java.util.List;

public class PostDTO {
    
    private String id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private String authorId;
    private String categoryId;
    private PostStatus status;
    private Integer likeCount;
    private Integer viewCount;
    private Integer commentCount;
    private Boolean isTop;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> tags;
    private QAResolveStatus resolveStatus;
    private LocalDateTime solvedAt;
    private org.xhy.community.domain.post.valueobject.CategoryType categoryType;
    
    public PostDTO() {
    }
    
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
    
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }
    
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
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public QAResolveStatus getResolveStatus() { return resolveStatus; }
    public void setResolveStatus(QAResolveStatus resolveStatus) { this.resolveStatus = resolveStatus; }

    public LocalDateTime getSolvedAt() { return solvedAt; }
    public void setSolvedAt(LocalDateTime solvedAt) { this.solvedAt = solvedAt; }

    public org.xhy.community.domain.post.valueobject.CategoryType getCategoryType() { return categoryType; }
    public void setCategoryType(org.xhy.community.domain.post.valueobject.CategoryType categoryType) { this.categoryType = categoryType; }
}
