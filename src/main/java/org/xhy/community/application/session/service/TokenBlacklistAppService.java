package org.xhy.community.application.session.service;

import org.springframework.stereotype.Service;
import org.xhy.community.domain.session.service.TokenBlacklistDomainService;

/**
 * Token 黑名单应用服务
 * 为接口层提供黑名单校验能力，避免直接依赖领域服务。
 */
@Service
public class TokenBlacklistAppService {

    private final TokenBlacklistDomainService tokenBlacklistDomainService;

    public TokenBlacklistAppService(TokenBlacklistDomainService tokenBlacklistDomainService) {
        this.tokenBlacklistDomainService = tokenBlacklistDomainService;
    }

    public boolean isBlacklisted(String token) {
        return tokenBlacklistDomainService.isBlacklisted(token);
    }
}

