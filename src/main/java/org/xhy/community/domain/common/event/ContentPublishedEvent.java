package org.xhy.community.domain.common.event;

import org.xhy.community.domain.common.valueobject.ContentType;

import java.time.LocalDateTime;

/**
 * 内容发布事件 - 简化版本
 * 只包含必要的标识信息，不包含复杂的业务数据
 * 由Application层的事件调度器处理后续的关注者查询和通知分发
 */
public class ContentPublishedEvent {

    private final ContentType contentType;      // 内容类型
    private final String contentId;             // 内容ID
    private final String authorId;              // 作者ID
    private final LocalDateTime publishTime;    // 发布时间

    public ContentPublishedEvent(ContentType contentType, String contentId, String authorId) {
        this.contentType = contentType;
        this.contentId = contentId;
        this.authorId = authorId;
        this.publishTime = LocalDateTime.now();
    }

    public ContentPublishedEvent(ContentType contentType, String contentId, String authorId, LocalDateTime publishTime) {
        this.contentType = contentType;
        this.contentId = contentId;
        this.authorId = authorId;
        this.publishTime = publishTime;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getContentId() {
        return contentId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    @Override
    public String toString() {
        return "ContentPublishedEvent{" +
                "contentType=" + contentType +
                ", contentId='" + contentId + '\'' +
                ", authorId='" + authorId + '\'' +
                ", publishTime=" + publishTime +
                '}';
    }
}