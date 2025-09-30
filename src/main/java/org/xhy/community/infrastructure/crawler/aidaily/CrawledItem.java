package org.xhy.community.infrastructure.crawler.aidaily;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 爬取到的原始条目
 */
public class CrawledItem {
    private String sourceName; // 例如 AIBASE
    private Long sourceItemId; // 原站自增ID
    private String title;
    private String summary;
    private String contentHtml;
    private String url;
    private LocalDateTime publishedAt;
    private Map<String, Object> metadata;

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public Long getSourceItemId() { return sourceItemId; }
    public void setSourceItemId(Long sourceItemId) { this.sourceItemId = sourceItemId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContentHtml() { return contentHtml; }
    public void setContentHtml(String contentHtml) { this.contentHtml = contentHtml; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}

