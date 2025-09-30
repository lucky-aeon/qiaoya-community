package org.xhy.community.infrastructure.crawler.aidaily;

import java.util.List;

/**
 * 通用爬虫客户端接口
 */
public interface CrawlerClient {
    List<CrawledItem> crawlIncremental(long startId, int maxConsecutive404, int maxRetries, long intervalMillis);

    default List<CrawledItem> crawlIncremental(long startId, int maxConsecutive404, int maxRetries, long intervalMillis, int maxCount) {
        // 默认实现：调用无上限版本，再裁剪数量
        List<CrawledItem> all = crawlIncremental(startId, maxConsecutive404, maxRetries, intervalMillis);
        if (maxCount > 0 && all.size() > maxCount) {
            return all.subList(0, maxCount);
        }
        return all;
    }
}
