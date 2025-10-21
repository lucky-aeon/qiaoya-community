package org.xhy.community.application.oauth2.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.oauth2.dto.OAuth2AuthorizeResponseDTO;
import org.xhy.community.application.oauth2.dto.OAuth2TokenDTO;
import org.xhy.community.domain.oauth2.entity.OAuth2AuthorizationEntity;
import org.xhy.community.domain.oauth2.valueobject.TokenType;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * OAuth2 授权转换器
 * 负责 Domain 实体到 DTO 的转换
 */
public class OAuth2AuthorizationAssembler {

    /**
     * 授权记录转换为 Token 响应
     */
    public static OAuth2TokenDTO toTokenDTO(OAuth2AuthorizationEntity authorization) {
        if (authorization == null) {
            return null;
        }

        OAuth2TokenDTO dto = new OAuth2TokenDTO();
        dto.setAccessToken(authorization.getAccessTokenValue());
        dto.setTokenType(TokenType.BEARER.getValue());
        dto.setScope(authorization.getAccessTokenScopes().replace(",", " "));

        // 计算剩余有效期
        if (authorization.getAccessTokenExpiresAt() != null) {
            long expiresIn = Duration.between(LocalDateTime.now(), authorization.getAccessTokenExpiresAt()).getSeconds();
            dto.setExpiresIn(Math.max(0, expiresIn));
        }

        // Refresh Token（客户端凭证模式不返回）
        if (authorization.getRefreshTokenValue() != null) {
            dto.setRefreshToken(authorization.getRefreshTokenValue());
        }

        return dto;
    }

    /**
     * 创建授权响应（授权码）
     */
    public static OAuth2AuthorizeResponseDTO toAuthorizeResponseDTO(String code, String state) {
        OAuth2AuthorizeResponseDTO dto = new OAuth2AuthorizeResponseDTO();
        dto.setCode(code);
        dto.setState(state);
        return dto;
    }
}
