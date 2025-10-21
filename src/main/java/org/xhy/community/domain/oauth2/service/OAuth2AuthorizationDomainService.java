package org.xhy.community.domain.oauth2.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.oauth2.entity.OAuth2AuthorizationConsentEntity;
import org.xhy.community.domain.oauth2.entity.OAuth2AuthorizationEntity;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.domain.oauth2.repository.OAuth2AuthorizationConsentRepository;
import org.xhy.community.domain.oauth2.repository.OAuth2AuthorizationRepository;
import org.xhy.community.domain.oauth2.valueobject.GrantType;
import org.xhy.community.domain.oauth2.valueobject.TokenType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.OAuth2ErrorCode;
import org.xhy.community.infrastructure.oauth.OAuth2TokenService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OAuth2 授权领域服务
 * 负责授权码、Token 的核心业务逻辑
 */
@Service
public class OAuth2AuthorizationDomainService {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthorizationDomainService.class);

    private final OAuth2AuthorizationRepository authorizationRepository;
    private final OAuth2AuthorizationConsentRepository consentRepository;
    private final OAuth2ClientDomainService clientDomainService;
    private final OAuth2TokenService tokenService;

    // 授权码默认有效期：10分钟
    private static final int AUTHORIZATION_CODE_VALIDITY_SECONDS = 600;

    public OAuth2AuthorizationDomainService(
            OAuth2AuthorizationRepository authorizationRepository,
            OAuth2AuthorizationConsentRepository consentRepository,
            OAuth2ClientDomainService clientDomainService,
            OAuth2TokenService tokenService) {
        this.authorizationRepository = authorizationRepository;
        this.consentRepository = consentRepository;
        this.clientDomainService = clientDomainService;
        this.tokenService = tokenService;
    }

    /**
     * 创建授权码
     * 用于授权码模式的第一步
     *
     * @param clientId 客户端ID
     * @param userId 用户ID
     * @param scopes 授权的权限范围
     * @param redirectUri 重定向URI
     * @param state CSRF state参数
     * @return 授权码
     */
    public String createAuthorizationCode(String clientId, String userId,
                                         List<String> scopes, String redirectUri, String state) {
        // 验证客户端
        OAuth2ClientEntity client = clientDomainService.getClientByClientId(clientId);
        validateClientActive(client);

        // 验证重定向URI
        if (!client.isValidRedirectUri(redirectUri)) {
            throw new BusinessException(OAuth2ErrorCode.INVALID_REDIRECT_URI);
        }

        // 验证授权类型
        if (!client.isGrantTypeSupported(GrantType.AUTHORIZATION_CODE.getValue())) {
            throw new BusinessException(OAuth2ErrorCode.INVALID_GRANT_TYPE);
        }

        // 验证 Scope
        for (String scope : scopes) {
            if (!client.isScopeAllowed(scope)) {
                throw new BusinessException(OAuth2ErrorCode.INVALID_SCOPE, "Scope不在允许范围内: " + scope);
            }
        }

        // 生成授权码
        String authorizationCode = tokenService.generateAuthorizationCode();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = tokenService.calculateExpiresAt(AUTHORIZATION_CODE_VALIDITY_SECONDS);

        // 创建授权记录
        OAuth2AuthorizationEntity authorization = new OAuth2AuthorizationEntity();
        authorization.setClientId(clientId);
        authorization.setPrincipalName(userId);
        authorization.setAuthorizationGrantType(GrantType.AUTHORIZATION_CODE.getValue());
        authorization.setAuthorizationCodeValue(authorizationCode);
        authorization.setAuthorizationCodeIssuedAt(now);
        authorization.setAuthorizationCodeExpiresAt(expiresAt);
        authorization.setAccessTokenScopes(String.join(",", scopes));
        authorization.setState(state);

        authorizationRepository.insert(authorization);

        return authorizationCode;
    }

    /**
     * 使用授权码换取 Access Token
     * 授权码模式的第二步
     *
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param authorizationCode 授权码
     * @param redirectUri 重定向URI（必须与授权时一致）
     * @return 授权记录（包含 Access Token 和 Refresh Token）
     */
    public OAuth2AuthorizationEntity exchangeAuthorizationCodeForToken(
            String clientId, String clientSecret, String authorizationCode, String redirectUri) {

        // 验证客户端
        OAuth2ClientEntity client = clientDomainService.getClientByClientId(clientId);
        validateClientActive(client);

        // 验证客户端密钥
        if (!clientDomainService.validateClientSecret(clientId, clientSecret)) {
            throw new BusinessException(OAuth2ErrorCode.INVALID_CLIENT_CREDENTIALS);
        }

        // 查询授权码记录
        LambdaQueryWrapper<OAuth2AuthorizationEntity> queryWrapper = new LambdaQueryWrapper<OAuth2AuthorizationEntity>()
                .eq(OAuth2AuthorizationEntity::getClientId, clientId)
                .eq(OAuth2AuthorizationEntity::getAuthorizationCodeValue, authorizationCode)
                .orderByDesc(OAuth2AuthorizationEntity::getCreateTime)
                .last("LIMIT 1");

        OAuth2AuthorizationEntity authorization = authorizationRepository.selectOne(queryWrapper);
        if (authorization == null) {
            throw new BusinessException(OAuth2ErrorCode.INVALID_AUTHORIZATION_CODE);
        }

        // 验证授权码是否过期
        if (!authorization.isAuthorizationCodeValid()) {
            throw new BusinessException(OAuth2ErrorCode.EXPIRED_AUTHORIZATION_CODE);
        }

        // 授权码使用后立即失效
        authorization.invalidateAuthorizationCode();

        // 生成 Access Token 和 Refresh Token
        String userId = authorization.getPrincipalName();
        List<String> scopes = List.of(authorization.getAccessTokenScopes().split(","));

        String accessToken = tokenService.generateAccessToken(
                userId, clientId, scopes, client.getAccessTokenValiditySeconds());
        String refreshToken = tokenService.generateRefreshToken();

        LocalDateTime now = LocalDateTime.now();
        authorization.setAccessTokenValue(accessToken);
        authorization.setAccessTokenIssuedAt(now);
        authorization.setAccessTokenExpiresAt(tokenService.calculateExpiresAt(client.getAccessTokenValiditySeconds()));
        authorization.setAccessTokenType(TokenType.BEARER.getValue());

        authorization.setRefreshTokenValue(refreshToken);
        authorization.setRefreshTokenIssuedAt(now);
        authorization.setRefreshTokenExpiresAt(tokenService.calculateExpiresAt(client.getRefreshTokenValiditySeconds()));

        // 删除该用户对该客户端的旧授权记录（保留当前授权）
        revokeOldAuthorizations(userId, clientId, authorization.getId());

        authorizationRepository.updateById(authorization);

        return authorization;
    }

    /**
     * 使用 Refresh Token 刷新 Access Token
     *
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param refreshToken Refresh Token
     * @return 授权记录（包含新的 Access Token 和 Refresh Token）
     */
    public OAuth2AuthorizationEntity refreshAccessToken(String clientId, String clientSecret, String refreshToken) {
        // 验证客户端
        OAuth2ClientEntity client = clientDomainService.getClientByClientId(clientId);
        validateClientActive(client);

        // 验证客户端密钥
        if (!clientDomainService.validateClientSecret(clientId, clientSecret)) {
            throw new BusinessException(OAuth2ErrorCode.INVALID_CLIENT_CREDENTIALS);
        }

        // 查询 Refresh Token 记录
        LambdaQueryWrapper<OAuth2AuthorizationEntity> queryWrapper = new LambdaQueryWrapper<OAuth2AuthorizationEntity>()
                .eq(OAuth2AuthorizationEntity::getClientId, clientId)
                .eq(OAuth2AuthorizationEntity::getRefreshTokenValue, refreshToken)
                .orderByDesc(OAuth2AuthorizationEntity::getCreateTime)
                .last("LIMIT 1");

        OAuth2AuthorizationEntity authorization = authorizationRepository.selectOne(queryWrapper);
        if (authorization == null) {
            throw new BusinessException(OAuth2ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 验证 Refresh Token 是否过期
        if (!authorization.isRefreshTokenValid()) {
            throw new BusinessException(OAuth2ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        // 生成新的 Access Token 和 Refresh Token
        String userId = authorization.getPrincipalName();
        List<String> scopes = List.of(authorization.getAccessTokenScopes().split(","));

        String newAccessToken = tokenService.generateAccessToken(
                userId, clientId, scopes, client.getAccessTokenValiditySeconds());
        String newRefreshToken = tokenService.generateRefreshToken();

        LocalDateTime now = LocalDateTime.now();
        authorization.setAccessTokenValue(newAccessToken);
        authorization.setAccessTokenIssuedAt(now);
        authorization.setAccessTokenExpiresAt(tokenService.calculateExpiresAt(client.getAccessTokenValiditySeconds()));

        authorization.setRefreshTokenValue(newRefreshToken);
        authorization.setRefreshTokenIssuedAt(now);
        authorization.setRefreshTokenExpiresAt(tokenService.calculateExpiresAt(client.getRefreshTokenValiditySeconds()));

        authorizationRepository.updateById(authorization);

        return authorization;
    }

    /**
     * 客户端凭证模式获取 Token
     * 用于服务端对服务端的授权
     *
     * @param clientId 客户端ID
     * @param clientSecret 客户端密钥
     * @param scopes 请求的权限范围
     * @return 授权记录（包含 Access Token）
     */
    public OAuth2AuthorizationEntity getTokenByClientCredentials(
            String clientId, String clientSecret, List<String> scopes) {

        // 验证客户端
        OAuth2ClientEntity client = clientDomainService.getClientByClientId(clientId);
        validateClientActive(client);

        // 验证客户端密钥
        if (!clientDomainService.validateClientSecret(clientId, clientSecret)) {
            throw new BusinessException(OAuth2ErrorCode.INVALID_CLIENT_CREDENTIALS);
        }

        // 验证授权类型
        if (!client.isGrantTypeSupported(GrantType.CLIENT_CREDENTIALS.getValue())) {
            throw new BusinessException(OAuth2ErrorCode.INVALID_GRANT_TYPE);
        }

        // 验证 Scope
        for (String scope : scopes) {
            if (!client.isScopeAllowed(scope)) {
                throw new BusinessException(OAuth2ErrorCode.INVALID_SCOPE, "Scope不在允许范围内: " + scope);
            }
        }

        // 生成 Access Token（客户端凭证模式不生成 Refresh Token）
        String accessToken = tokenService.generateAccessToken(
                clientId, clientId, scopes, client.getAccessTokenValiditySeconds());

        LocalDateTime now = LocalDateTime.now();
        OAuth2AuthorizationEntity authorization = new OAuth2AuthorizationEntity();
        authorization.setClientId(clientId);
        authorization.setPrincipalName(clientId); // 客户端凭证模式使用 clientId 作为 principal
        authorization.setAuthorizationGrantType(GrantType.CLIENT_CREDENTIALS.getValue());
        authorization.setAccessTokenValue(accessToken);
        authorization.setAccessTokenIssuedAt(now);
        authorization.setAccessTokenExpiresAt(tokenService.calculateExpiresAt(client.getAccessTokenValiditySeconds()));
        authorization.setAccessTokenType(TokenType.BEARER.getValue());
        authorization.setAccessTokenScopes(String.join(",", scopes));

        // 客户端凭证模式：删除该客户端的旧授权（每个客户端只保留一个凭证授权）
        LambdaQueryWrapper<OAuth2AuthorizationEntity> deleteWrapper =
            new LambdaQueryWrapper<OAuth2AuthorizationEntity>()
                .eq(OAuth2AuthorizationEntity::getPrincipalName, clientId)
                .eq(OAuth2AuthorizationEntity::getClientId, clientId)
                .eq(OAuth2AuthorizationEntity::getAuthorizationGrantType, GrantType.CLIENT_CREDENTIALS.getValue());
        authorizationRepository.delete(deleteWrapper);

        authorizationRepository.insert(authorization);

        return authorization;
    }

    /**
     * 保存用户授权同意
     *
     * @param clientId 客户端ID
     * @param userId 用户ID
     * @param scopes 授权的权限范围
     */
    public void saveConsent(String clientId, String userId, List<String> scopes) {
        // 检查是否已有授权同意记录
        LambdaQueryWrapper<OAuth2AuthorizationConsentEntity> queryWrapper =
                new LambdaQueryWrapper<OAuth2AuthorizationConsentEntity>()
                .eq(OAuth2AuthorizationConsentEntity::getClientId, clientId)
                .eq(OAuth2AuthorizationConsentEntity::getPrincipalName, userId);

        OAuth2AuthorizationConsentEntity existing = consentRepository.selectOne(queryWrapper);

        if (existing != null) {
            // 更新已有记录
            existing.setAuthorities(String.join(",", scopes));
            existing.setConsentTime(LocalDateTime.now());
            consentRepository.updateById(existing);
        } else {
            // 创建新记录
            OAuth2AuthorizationConsentEntity consent = new OAuth2AuthorizationConsentEntity();
            consent.setClientId(clientId);
            consent.setPrincipalName(userId);
            consent.setAuthorities(String.join(",", scopes));
            consentRepository.insert(consent);
        }
    }

    /**
     * 检查用户是否已授权同意
     *
     * @param clientId 客户端ID
     * @param userId 用户ID
     * @return 已授权的权限范围，如果未授权则返回 null
     */
    public List<String> getConsentedScopes(String clientId, String userId) {
        LambdaQueryWrapper<OAuth2AuthorizationConsentEntity> queryWrapper =
                new LambdaQueryWrapper<OAuth2AuthorizationConsentEntity>()
                .eq(OAuth2AuthorizationConsentEntity::getClientId, clientId)
                .eq(OAuth2AuthorizationConsentEntity::getPrincipalName, userId);

        OAuth2AuthorizationConsentEntity consent = consentRepository.selectOne(queryWrapper);
        if (consent != null) {
            return List.of(consent.getAuthorities().split(","));
        }
        return null;
    }

    /**
     * 获取用户的所有有效授权列表（分页）
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户的授权分页列表
     */
    public IPage<OAuth2AuthorizationEntity> getUserAuthorizations(String userId, Integer pageNum, Integer pageSize) {
        Page<OAuth2AuthorizationEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<OAuth2AuthorizationEntity> queryWrapper =
            new LambdaQueryWrapper<OAuth2AuthorizationEntity>()
                .eq(OAuth2AuthorizationEntity::getPrincipalName, userId)
                .isNotNull(OAuth2AuthorizationEntity::getAccessTokenValue)
                .orderByDesc(OAuth2AuthorizationEntity::getAccessTokenIssuedAt);

        return authorizationRepository.selectPage(page, queryWrapper);
    }

    /**
     * 撤销用户对某个应用的授权
     *
     * @param userId 用户ID
     * @param clientId 客户端ID
     */
    public void revokeAuthorization(String userId, String clientId) {
        // 删除所有该用户对该客户端的授权记录
        LambdaQueryWrapper<OAuth2AuthorizationEntity> authQueryWrapper =
            new LambdaQueryWrapper<OAuth2AuthorizationEntity>()
                .eq(OAuth2AuthorizationEntity::getPrincipalName, userId)
                .eq(OAuth2AuthorizationEntity::getClientId, clientId);

        authorizationRepository.delete(authQueryWrapper);

        // 删除授权同意记录
        LambdaQueryWrapper<OAuth2AuthorizationConsentEntity> consentQueryWrapper =
            new LambdaQueryWrapper<OAuth2AuthorizationConsentEntity>()
                .eq(OAuth2AuthorizationConsentEntity::getPrincipalName, userId)
                .eq(OAuth2AuthorizationConsentEntity::getClientId, clientId);

        consentRepository.delete(consentQueryWrapper);
    }

    /**
     * 清理过期的授权记录
     * 由定时任务调用，清理已过期的 Token 和授权码
     */
    public void cleanupExpiredAuthorizations() {
        LocalDateTime now = LocalDateTime.now();

        // 规则1：清理 Access Token 和 Refresh Token 都已过期的记录
        LambdaQueryWrapper<OAuth2AuthorizationEntity> expiredTokenWrapper =
            new LambdaQueryWrapper<OAuth2AuthorizationEntity>()
                .isNotNull(OAuth2AuthorizationEntity::getAccessTokenValue)
                .lt(OAuth2AuthorizationEntity::getAccessTokenExpiresAt, now)
                .lt(OAuth2AuthorizationEntity::getRefreshTokenExpiresAt, now);

        // 规则2：清理只有授权码且已过期的记录（未换取 Token）
        LambdaQueryWrapper<OAuth2AuthorizationEntity> expiredCodeWrapper =
            new LambdaQueryWrapper<OAuth2AuthorizationEntity>()
                .isNull(OAuth2AuthorizationEntity::getAccessTokenValue)
                .isNotNull(OAuth2AuthorizationEntity::getAuthorizationCodeValue)
                .lt(OAuth2AuthorizationEntity::getAuthorizationCodeExpiresAt, now);

        int deletedTokens = authorizationRepository.delete(expiredTokenWrapper);
        int deletedCodes = authorizationRepository.delete(expiredCodeWrapper);

        log.info("OAuth2 授权清理完成: 过期Token={}, 过期授权码={}", deletedTokens, deletedCodes);
    }

    /**
     * 验证客户端是否处于激活状态
     */
    private void validateClientActive(OAuth2ClientEntity client) {
        if (!client.isActive()) {
            if (client.getStatus().name().equals("SUSPENDED")) {
                throw new BusinessException(OAuth2ErrorCode.CLIENT_SUSPENDED);
            } else {
                throw new BusinessException(OAuth2ErrorCode.CLIENT_REVOKED);
            }
        }
    }

    /**
     * 删除用户对客户端的旧授权记录（保留当前授权）
     * 避免同一用户对同一客户端积累多条授权记录
     *
     * @param userId 用户ID
     * @param clientId 客户端ID
     * @param currentAuthorizationId 当前授权ID（需要保留的授权）
     */
    private void revokeOldAuthorizations(String userId, String clientId, String currentAuthorizationId) {
        LambdaQueryWrapper<OAuth2AuthorizationEntity> queryWrapper =
            new LambdaQueryWrapper<OAuth2AuthorizationEntity>()
                .eq(OAuth2AuthorizationEntity::getPrincipalName, userId)
                .eq(OAuth2AuthorizationEntity::getClientId, clientId)
                .ne(OAuth2AuthorizationEntity::getId, currentAuthorizationId)
                .isNotNull(OAuth2AuthorizationEntity::getAccessTokenValue);

        authorizationRepository.delete(queryWrapper);
    }
}
