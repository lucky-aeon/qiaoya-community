package org.xhy.community.infrastructure.crawler.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xhy.community.application.ainews.service.AibaseIngestAppService;

@Component
public class AibaseIngestScheduler {

    private static final Logger log = LoggerFactory.getLogger(AibaseIngestScheduler.class);

    private final AibaseIngestAppService aibaseIngestAppService;

    public AibaseIngestScheduler(AibaseIngestAppService aibaseIngestAppService) {
        this.aibaseIngestAppService = aibaseIngestAppService;
    }

    /**
     * 每小时采集一次（在每小时的第0分钟触发）
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void hourlyIngest() {
        try {
            AibaseIngestAppService.IngestResult r = aibaseIngestAppService.ingestLatest();
            log.info("[AIBase] ingest finished: startId={} fetched={} inserted={}", r.getStartId(), r.getFetched(), r.getInserted());
        } catch (Exception e) {
            log.warn("[AIBase] ingest failed", e);
        }
    }
}
