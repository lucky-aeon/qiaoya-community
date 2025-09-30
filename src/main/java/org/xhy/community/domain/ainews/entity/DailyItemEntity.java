package org.xhy.community.domain.ainews.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.ainews.valueobject.DailyItemStatus;
import org.xhy.community.domain.ainews.valueobject.DailySource;
import org.xhy.community.domain.common.entity.BaseEntity;
import org.xhy.community.infrastructure.converter.MapJsonTypeHandler;
import org.xhy.community.infrastructure.converter.DailySourceConverter;
import org.xhy.community.infrastructure.converter.DailyItemStatusConverter;

import java.time.LocalDateTime;
import java.util.Map;

@TableName("ai_daily_items")
public class DailyItemEntity extends BaseEntity {

    @TableField(typeHandler = DailySourceConverter.class)
    private DailySource source;
    private String title;
    private String summary;
    private String content;
    private String url;
    private Long sourceItemId;
    private LocalDateTime publishedAt;
    private LocalDateTime fetchedAt;
    private String urlHash;
    @TableField(typeHandler = DailyItemStatusConverter.class)
    private DailyItemStatus status;

    @TableField(value = "metadata", typeHandler = MapJsonTypeHandler.class)
    private Map<String, Object> metadata;

    public DailySource getSource() { return source; }
    public void setSource(DailySource source) { this.source = source; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Long getSourceItemId() { return sourceItemId; }
    public void setSourceItemId(Long sourceItemId) { this.sourceItemId = sourceItemId; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public LocalDateTime getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }

    public String getUrlHash() { return urlHash; }
    public void setUrlHash(String urlHash) { this.urlHash = urlHash; }

    public DailyItemStatus getStatus() { return status; }
    public void setStatus(DailyItemStatus status) { this.status = status; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
