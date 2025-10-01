package org.xhy.community.application.ainews;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.xhy.community.application.ainews.service.AibaseIngestAppService;

@SpringBootTest
@ActiveProfiles("dev")
public class AibaseIngestManualTest {

    private static final Logger log = LoggerFactory.getLogger(AibaseIngestManualTest.class);

    @Autowired
    private AibaseIngestAppService aibaseIngestAppService;

    /**
     * 手动触发采集：读取系统属性 "ainews.limit" 指定条数，默认 3 条
     * 运行方式示例：
     * mvn -Dtest=AibaseIngestManualTest#ingestWithLimit -Dainews.limit=5 -DskipTests=false test
     */
    @Test
    public void ingestWithLimit() {
        int limit = 20;
        try {
            String prop = System.getProperty("ainews.limit");
            if (prop != null && !prop.isBlank()) {
                limit = Integer.parseInt(prop.trim());
            }
        } catch (Exception ignored) { }

        log.info("[TEST] 手动触发 AIBase 采集，限制条数：{}", limit);
        var result = aibaseIngestAppService.ingestLatest(limit);
        log.info("[TEST] 采集完成：startId={}, fetched={}, inserted={}", result.getStartId(), result.getFetched(), result.getInserted());
    }
}

