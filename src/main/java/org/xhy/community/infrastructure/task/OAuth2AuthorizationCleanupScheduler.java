package org.xhy.community.infrastructure.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.oauth2.service.OAuth2AuthorizationDomainService;

/**
 * OAuth2 授权清理定时任务
 * 每天凌晨 2 点清理过期的授权记录
 */
@Component
public class OAuth2AuthorizationCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthorizationCleanupScheduler.class);

    private final OAuth2AuthorizationDomainService authorizationDomainService;

    public OAuth2AuthorizationCleanupScheduler(OAuth2AuthorizationDomainService authorizationDomainService) {
        this.authorizationDomainService = authorizationDomainService;
    }

    /**
     * 每天凌晨 2 点执行清理任务
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredAuthorizations() {
        try {
            log.info("开始执行 OAuth2 授权清理任务");
            authorizationDomainService.cleanupExpiredAuthorizations();
            log.info("OAuth2 授权清理任务执行完成");
        } catch (Exception e) {
            log.error("OAuth2 授权清理任务执行失败", e);
        }
    }
}
