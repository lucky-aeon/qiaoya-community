package org.xhy.community.application.oauth2.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.oauth2.assembler.OAuth2AuthorizationAssembler;
import org.xhy.community.application.oauth2.dto.OAuth2TokenDTO;
import org.xhy.community.domain.oauth2.entity.OAuth2AuthorizationEntity;
import org.xhy.community.domain.oauth2.service.OAuth2AuthorizationDomainService;
import org.xhy.community.domain.oauth2.valueobject.GrantType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.OAuth2ErrorCode;
import org.xhy.community.interfaces.oauth2.request.OAuth2TokenRequest;

import java.util.Arrays;
import java.util.List;

/**
 * OAuth2 授权应用服务
 * 编排 OAuth2 授权流程
 */
@Service
public class OAuth2AuthorizationAppService {

    private final OAuth2AuthorizationDomainService authorizationDomainService;

    public OAuth2AuthorizationAppService(OAuth2AuthorizationDomainService authorizationDomainService) {
        this.authorizationDomainService = authorizationDomainService;
    }

    /**
     * 生成授权码
     * 授权码模式第一步：用户同意授权后生成授权码
     *
     * @param clientId 客户端ID
     * @param userId 用户ID
     * @param scopes 授权的权限范围列表
     * @param redirectUri 重定向URI
     * @param state State 参数
     * @return 授权码
     */
    public String generateAuthorizationCode(String clientId, String userId,
                                            List<String> scopes, String redirectUri, String state) {
        return authorizationDomainService.createAuthorizationCode(clientId, userId, scopes, redirectUri, state);
    }

    /**
     * 获取 Token
     * 根据不同的 grant_type 处理不同的授权流程
     *
     * @param request Token 请求参数
     * @return Token 响应
     */
    public OAuth2TokenDTO getToken(OAuth2TokenRequest request) {
        String grantType = request.getGrantType();

        OAuth2AuthorizationEntity authorization;

        // 根据授权类型处理
        if (GrantType.AUTHORIZATION_CODE.getValue().equals(grantType)) {
            // 授权码模式
            authorization = authorizationDomainService.exchangeAuthorizationCodeForToken(
                    request.getClientId(),
                    request.getClientSecret(),
                    request.getCode(),
                    request.getRedirectUri()
            );
        } else if (GrantType.REFRESH_TOKEN.getValue().equals(grantType)) {
            // 刷新令牌模式
            authorization = authorizationDomainService.refreshAccessToken(
                    request.getClientId(),
                    request.getClientSecret(),
                    request.getRefreshToken()
            );
        } else if (GrantType.CLIENT_CREDENTIALS.getValue().equals(grantType)) {
            // 客户端凭证模式
            List<String> scopes = request.getScope() != null
                    ? Arrays.asList(request.getScope().split(" "))
                    : List.of();
            authorization = authorizationDomainService.getTokenByClientCredentials(
                    request.getClientId(),
                    request.getClientSecret(),
                    scopes
            );
        } else {
            throw new BusinessException(OAuth2ErrorCode.INVALID_GRANT_TYPE);
        }

        return OAuth2AuthorizationAssembler.toTokenDTO(authorization);
    }

    /**
     * 保存用户授权同意
     *
     * @param clientId 客户端ID
     * @param userId 用户ID
     * @param scopes 授权的权限范围
     */
    public void saveConsent(String clientId, String userId, List<String> scopes) {
        authorizationDomainService.saveConsent(clientId, userId, scopes);
    }

    /**
     * 检查用户是否已授权同意
     *
     * @param clientId 客户端ID
     * @param userId 用户ID
     * @return 已授权的权限范围，如果未授权则返回 null
     */
    public List<String> getConsentedScopes(String clientId, String userId) {
        return authorizationDomainService.getConsentedScopes(clientId, userId);
    }
}
