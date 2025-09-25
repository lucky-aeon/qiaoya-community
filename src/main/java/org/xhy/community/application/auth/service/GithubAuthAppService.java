package org.xhy.community.application.auth.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.auth.assembler.AuthAssembler;
import org.xhy.community.application.auth.dto.AuthDTO;
import org.xhy.community.application.auth.dto.AuthorizeUrlDTO;
import org.xhy.community.application.auth.dto.UserSocialBindStatusDTO;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.domain.auth.entity.UserSocialAccountEntity;
import org.xhy.community.domain.auth.service.AuthDomainService;
import org.xhy.community.domain.auth.valueobject.OpenIdProfile;
import org.xhy.community.domain.common.valueobject.AuthProvider;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.GithubOAuthConfig;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.infrastructure.config.JwtUtil;
import org.xhy.community.infrastructure.exception.AuthErrorCode;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.oauth.GithubOAuthClient;
import org.xhy.community.infrastructure.oauth.OAuthStateService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

@Service
public class GithubAuthAppService {

    private final SystemConfigDomainService systemConfigDomainService;
    private final OAuthStateService stateService;
    private final GithubOAuthClient githubOAuthClient;
    private final AuthDomainService authDomainService;
    private final JwtUtil jwtUtil;

    public GithubAuthAppService(SystemConfigDomainService systemConfigDomainService,
                                OAuthStateService stateService,
                                GithubOAuthClient githubOAuthClient,
                                AuthDomainService authDomainService,
                                JwtUtil jwtUtil) {
        this.systemConfigDomainService = systemConfigDomainService;
        this.stateService = stateService;
        this.githubOAuthClient = githubOAuthClient;
        this.authDomainService = authDomainService;
        this.jwtUtil = jwtUtil;
    }

    public AuthorizeUrlDTO getAuthorizeUrl() {
        GithubOAuthConfig cfg = systemConfigDomainService.getConfigData(SystemConfigType.OAUTH_GITHUB, GithubOAuthConfig.class);
        if (cfg == null || !cfg.isValid()) {
            throw new BusinessException(AuthErrorCode.OAUTH_CODE_EXCHANGE_FAILED, "GitHub OAuth 未配置");
        }
        String state = stateService.generateState();
        StringJoiner sj = new StringJoiner("&", cfg.getAuthorizeBaseUri() + "?", "");
        sj.add("client_id=" + url(cfg.getClientId()));
        sj.add("redirect_uri=" + url(cfg.getRedirectUri()));
        sj.add("scope=" + url(String.join(" ", cfg.getScopes())));
        sj.add("state=" + url(state));
        String url = sj.toString();
        long expireAt = System.currentTimeMillis() + 5 * 60 * 1000;
        return new AuthorizeUrlDTO(url, state, expireAt);
    }

    public AuthDTO handleCallback(String code, String state) {
        if (!stateService.validateState(state)) {
            throw new BusinessException(AuthErrorCode.OAUTH_STATE_INVALID);
        }
        GithubOAuthConfig cfg = systemConfigDomainService.getConfigData(SystemConfigType.OAUTH_GITHUB, GithubOAuthConfig.class);
        if (cfg == null || !cfg.isValid()) {
            throw new BusinessException(AuthErrorCode.OAUTH_CODE_EXCHANGE_FAILED, "GitHub OAuth 未配置");
        }

        // 兑换 token & 获取用户信息
        GithubOAuthClient.TokenResponse token = githubOAuthClient.exchangeCodeForToken(
                cfg.getTokenUri(), cfg.getClientId(), cfg.getClientSecret(), code, cfg.getRedirectUri());
        GithubOAuthClient.GithubUser gu = githubOAuthClient.fetchUserInfo(
                cfg.getUserApi(), cfg.getEmailApi(), token.getAccessToken(), cfg.isFetchEmailFromApi());

        // 组装 profile（合并策略由配置决定）
        OpenIdProfile profile = new OpenIdProfile();
        profile.setProvider(AuthProvider.GITHUB);
        profile.setOpenId(gu.id);
        profile.setLogin(gu.login);
        profile.setName(gu.name);
        profile.setAvatarUrl(gu.avatarUrl);
        profile.setEmail(gu.email);
        profile.setEmailVerified(gu.emailVerified);
        profile.setAllowMergeByEmail(!cfg.isRequireVerifiedEmailForMerge() || gu.emailVerified);

        UserEntity user = authDomainService.getOrCreateUserByGithub(profile);
        String tokenStr = jwtUtil.generateToken(user.getId(), user.getEmail());
        long expireAt = System.currentTimeMillis() + 24 * 60 * 60 * 1000; // follow JwtProperties default
        UserDTO userDTO = AuthAssembler.toUserDTO(user);

        AuthDTO dto = new AuthDTO();
        dto.setToken(tokenStr);
        dto.setExpireAt(expireAt);
        dto.setUser(userDTO);
        // 业务上是否新用户/合并可在上层计算，这里简单不标
        return dto;
    }

    public UserSocialBindStatusDTO getGithubBindStatus(String userId) {
        UserSocialAccountEntity bind = authDomainService.getGithubBindingByUserId(userId);
        return AuthAssembler.toBindStatusDTO(bind);
    }

    public UserSocialBindStatusDTO bindGithub(String userId, String code, String state) {
        if (!stateService.validateState(state)) {
            throw new BusinessException(AuthErrorCode.OAUTH_STATE_INVALID);
        }
        GithubOAuthConfig cfg = systemConfigDomainService.getConfigData(SystemConfigType.OAUTH_GITHUB, GithubOAuthConfig.class);
        if (cfg == null || !cfg.isValid()) {
            throw new BusinessException(AuthErrorCode.OAUTH_CODE_EXCHANGE_FAILED, "GitHub OAuth 未配置");
        }
        GithubOAuthClient.TokenResponse token = githubOAuthClient.exchangeCodeForToken(
                cfg.getTokenUri(), cfg.getClientId(), cfg.getClientSecret(), code, cfg.getRedirectUri());
        GithubOAuthClient.GithubUser gu = githubOAuthClient.fetchUserInfo(
                cfg.getUserApi(), cfg.getEmailApi(), token.getAccessToken(), cfg.isFetchEmailFromApi());

        OpenIdProfile profile = new OpenIdProfile();
        profile.setProvider(AuthProvider.GITHUB);
        profile.setOpenId(gu.id);
        profile.setLogin(gu.login);
        profile.setName(gu.name);
        profile.setAvatarUrl(gu.avatarUrl);
        profile.setEmail(gu.email);
        profile.setEmailVerified(gu.emailVerified);
        profile.setAllowMergeByEmail(false); // 绑定场景不走合并

        authDomainService.bindGithub(userId, profile);
        return getGithubBindStatus(userId);
    }

    public void unbindGithub(String userId) {
        authDomainService.unbindGithubByUserId(userId);
    }

    private String url(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }
}
