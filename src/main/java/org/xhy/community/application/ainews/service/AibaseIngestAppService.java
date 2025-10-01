package org.xhy.community.application.ainews.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.ainews.entity.DailyItemEntity;
import org.xhy.community.domain.ainews.service.DailyItemDomainService;
import org.xhy.community.domain.ainews.valueobject.DailyItemStatus;
import org.xhy.community.domain.ainews.valueobject.DailySource;
import org.xhy.community.infrastructure.crawler.aidaily.CrawledItem;
import org.xhy.community.infrastructure.crawler.aidaily.CrawlerClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

@Service
public class AibaseIngestAppService {

    private static final Logger log = LoggerFactory.getLogger(AibaseIngestAppService.class);

    private final DailyItemDomainService dailyItemDomainService;
    private final CrawlerClient aibaseCrawlerClient;

    // 配置：默认起始ID（参考旧社区实现）
    private static final long DEFAULT_START_ID = 18330L;
    private static final int MAX_CONSECUTIVE_404 = 20;
    private static final int MAX_RETRIES = 3;
    private static final long INTERVAL_MILLIS = 1000L;

    public AibaseIngestAppService(DailyItemDomainService dailyItemDomainService,
                                  CrawlerClient aibaseCrawlerClient) {
        this.dailyItemDomainService = dailyItemDomainService;
        this.aibaseCrawlerClient = aibaseCrawlerClient;
    }

    public IngestResult ingestLatest() {
        Long maxId = dailyItemDomainService.getMaxSourceItemId(DailySource.AIBASE);
        long start = (maxId == null ? DEFAULT_START_ID : maxId) + 1;
        log.info("[AIBase] start incremental crawling from id={}", start);

        List<CrawledItem> items = aibaseCrawlerClient.crawlIncremental(start, MAX_CONSECUTIVE_404, MAX_RETRIES, INTERVAL_MILLIS);
        List<DailyItemEntity> entities = new ArrayList<>();
        for (CrawledItem i : items) {
            DailyItemEntity e = new DailyItemEntity();
            e.setSource(DailySource.AIBASE);
            e.setSourceItemId(i.getSourceItemId());
            e.setTitle(i.getTitle());
            e.setContent(i.getContentHtml());
            e.setSummary(i.getSummary());
            e.setUrl(i.getUrl());
            e.setPublishedAt(i.getPublishedAt());
            e.setFetchedAt(LocalDateTime.now());
            e.setStatus(DailyItemStatus.PUBLISHED); // 默认公开
            e.setUrlHash(md5Hex(i.getUrl()));
            e.setMetadata(i.getMetadata());
            entities.add(e);
        }

        int inserted = dailyItemDomainService.upsertItems(entities);
        return new IngestResult(start, items.size(), inserted);
    }

    /**
     * 手动触发（带条数限制）
     * @param maxCount 最多抓取条数（<=0 表示不限）
     */
    public IngestResult ingestLatest(int maxCount) {
        Long maxId = dailyItemDomainService.getMaxSourceItemId(DailySource.AIBASE);
        long start = 21692;
        log.info("[AIBase] start incremental crawling with limit: id={}, maxCount={}", start, maxCount);

        List<CrawledItem> items = aibaseCrawlerClient.crawlIncremental(start, MAX_CONSECUTIVE_404, MAX_RETRIES, INTERVAL_MILLIS, maxCount);
        List<DailyItemEntity> entities = new ArrayList<>();
        for (CrawledItem i : items) {
            DailyItemEntity e = new DailyItemEntity();
            e.setSource(DailySource.AIBASE);
            e.setSourceItemId(i.getSourceItemId());
            e.setTitle(i.getTitle());
            e.setContent(i.getContentHtml());
            e.setSummary(i.getSummary());
            e.setUrl(i.getUrl());
            e.setPublishedAt(i.getPublishedAt());
            e.setFetchedAt(java.time.LocalDateTime.now());
            e.setStatus(DailyItemStatus.PUBLISHED);
            e.setUrlHash(md5Hex(i.getUrl()));
            e.setMetadata(i.getMetadata());
            entities.add(e);
        }

        int inserted = dailyItemDomainService.upsertItems(entities);
        return new IngestResult(start, items.size(), inserted);
    }

    private String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            try (Formatter fmt = new Formatter()) {
                for (byte b : digest) fmt.format("%02x", b);
                return fmt.toString();
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not found", e);
        }
    }

    public static class IngestResult {
        private final long startId;
        private final int fetched;
        private final int inserted;

        public IngestResult(long startId, int fetched, int inserted) {
            this.startId = startId;
            this.fetched = fetched;
            this.inserted = inserted;
        }

        public long getStartId() { return startId; }
        public int getFetched() { return fetched; }
        public int getInserted() { return inserted; }
    }
}
