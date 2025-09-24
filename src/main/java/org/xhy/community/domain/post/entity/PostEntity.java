package org.xhy.community.domain.post.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.infrastructure.converter.PostStatusConverter;
import org.xhy.community.infrastructure.converter.UniversalListConverter;

import java.time.LocalDateTime;
import java.util.List;

@TableName("posts")
public class PostEntity extends BaseEntity {
    
    /** 文章标题 */
    private String title;
    
    /** 文章内容 */
    private String content;
    
    /** 文章摘要/简介 */
    private String summary;
    
    /** 文章封面图片地址 */
    private String coverImage;
    
    /** 作者用户ID */
    private String authorId;
    
    /** 所属分类ID */
    private String categoryId;
    
    /** 文章状态：DRAFT-草稿，PUBLISHED-已发布 */
    @TableField(typeHandler = PostStatusConverter.class)
    private PostStatus status;
    
    /** 点赞数 */
    private Integer likeCount;
    
    /** 浏览次数 */
    private Integer viewCount;
    
    /** 评论数 */
    private Integer commentCount;
    
    /** 是否置顶 */
    private Boolean isTop;

    /** 标签，JSON格式存储 */
    @TableField(typeHandler = UniversalListConverter.class)
    private List<String> tags;
    
    /** 发布时间 */
    private LocalDateTime publishTime;
    
    public PostEntity() {
    }
    
    public PostEntity(String title, String content, String authorId, String categoryId) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.categoryId = categoryId;
        this.status = PostStatus.PUBLISHED;
        this.likeCount = 0;
        this.viewCount = 0;
        this.commentCount = 0;
        this.isTop = false;
    }
    
    // Getters and Setters
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

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
