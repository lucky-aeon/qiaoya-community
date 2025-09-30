package org.xhy.community.infrastructure.crawler.aidaily;

import java.util.List;

/**
 * 通用爬虫客户端接口
 */
public interface CrawlerClient {
    List<CrawledItem> crawlIncremental(long startId, int maxConsecutive404, int maxRetries, long intervalMillis);
}

