package org.xhy.community.domain.oauth2.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.xhy.community.domain.oauth2.entity.OAuth2AuthorizationEntity;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.domain.oauth2.repository.OAuth2AuthorizationConsentRepository;
import org.xhy.community.domain.oauth2.repository.OAuth2AuthorizationRepository;
import org.xhy.community.domain.oauth2.valueobject.ClientAuthenticationMethod;
import org.xhy.community.domain.oauth2.valueobject.GrantType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.OAuth2ErrorCode;
import org.xhy.community.infrastructure.oauth.OAuth2TokenService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2AuthorizationDomainServiceTest {

    @Test
    void publicClientUsesPkceWithoutClientSecret() {
        OAuth2AuthorizationRepository authorizationRepository = mock(OAuth2AuthorizationRepository.class);
        OAuth2ClientDomainService clientDomainService = mock(OAuth2ClientDomainService.class);
        OAuth2TokenService tokenService = mock(OAuth2TokenService.class);
        OAuth2AuthorizationDomainService service = new OAuth2AuthorizationDomainService(
                authorizationRepository,
                mock(OAuth2AuthorizationConsentRepository.class),
                clientDomainService,
                tokenService
        );

        OAuth2ClientEntity client = qiaoyaCliClient();
        when(clientDomainService.getClientByClientId("qiaoya-cli")).thenReturn(client);
        when(tokenService.generateAuthorizationCode()).thenReturn("auth-code");
        when(tokenService.calculateExpiresAt(anyInt())).thenAnswer(invocation ->
                LocalDateTime.now().plusSeconds(invocation.getArgument(0, Integer.class)));

        String verifier = "test-verifier-1234567890";
        String challenge = s256(verifier);
        String redirectUri = "http://127.0.0.1:49152/callback";

        service.createAuthorizationCode(
                "qiaoya-cli",
                "user-1",
                List.of("openid", "read"),
                redirectUri,
                "state-1",
                challenge,
                "S256"
        );

        ArgumentCaptor<OAuth2AuthorizationEntity> insertCaptor = ArgumentCaptor.forClass(OAuth2AuthorizationEntity.class);
        verify(authorizationRepository).insert(insertCaptor.capture());
        OAuth2AuthorizationEntity authorization = insertCaptor.getValue();
        when(authorizationRepository.selectOne(any())).thenReturn(authorization);
        when(tokenService.generateAccessToken(eq("user-1"), eq("qiaoya-cli"), any(), anyInt())).thenReturn("access-token");
        when(tokenService.generateRefreshToken()).thenReturn("refresh-token");

        OAuth2AuthorizationEntity exchanged = service.exchangeAuthorizationCodeForToken(
                "qiaoya-cli",
                null,
                "auth-code",
                redirectUri,
                verifier
        );

        assertEquals("access-token", exchanged.getAccessTokenValue());
        assertEquals("refresh-token", exchanged.getRefreshTokenValue());
        assertNull(exchanged.getAuthorizationCodeValue());
        verify(authorizationRepository).updateById(exchanged);
    }

    @Test
    void publicClientRequiresPkceChallenge() {
        OAuth2AuthorizationDomainService service = new OAuth2AuthorizationDomainService(
                mock(OAuth2AuthorizationRepository.class),
                mock(OAuth2AuthorizationConsentRepository.class),
                mockClientDomainService(qiaoyaCliClient()),
                mock(OAuth2TokenService.class)
        );

        BusinessException ex = assertThrows(BusinessException.class, () -> service.createAuthorizationCode(
                "qiaoya-cli",
                "user-1",
                List.of("openid"),
                "http://127.0.0.1:49152/callback",
                "state-1",
                null,
                null
        ));

        assertEquals(OAuth2ErrorCode.INVALID_PKCE_CHALLENGE.getCode(), ex.getCode());
    }

    private OAuth2ClientDomainService mockClientDomainService(OAuth2ClientEntity client) {
        OAuth2ClientDomainService clientDomainService = mock(OAuth2ClientDomainService.class);
        when(clientDomainService.getClientByClientId("qiaoya-cli")).thenReturn(client);
        return clientDomainService;
    }

    private OAuth2ClientEntity qiaoyaCliClient() {
        OAuth2ClientEntity client = new OAuth2ClientEntity();
        client.setClientId("qiaoya-cli");
        client.setClientAuthenticationMethods(List.of(ClientAuthenticationMethod.NONE.getValue()));
        client.setGrantTypes(List.of(GrantType.AUTHORIZATION_CODE.getValue(), GrantType.REFRESH_TOKEN.getValue()));
        client.setScopes(List.of("openid", "profile", "email", "read"));
        client.setRedirectUris(List.of("http://127.0.0.1/callback", "http://localhost/callback"));
        client.setRequireProofKey(true);
        return client;
    }

    private String s256(String verifier) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
