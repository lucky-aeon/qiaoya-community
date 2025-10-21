package org.xhy.community.infrastructure.oauth;

import org.springframework.stereotype.Service;
import org.xhy.community.domain.oauth2.valueobject.TokenType;
import org.xhy.community.infrastructure.config.JwtUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 Token 生成服务
 * 负责生成授权码、Access Token、Refresh Token
 */
@Service
public class OAuth2TokenService {

    private final JwtUtil jwtUtil;
    private final SecureRandom secureRandom;

    public OAuth2TokenService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.secureRandom = new SecureRandom();
    }

    /**
     * 生成授权码
     * 使用安全随机数生成 32 字节的授权码
     */
    public String generateAuthorizationCode() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 生成 Access Token (JWT 格式)
     *
     * @param userId 用户ID
     * @param clientId 客户端ID
     * @param scopes 权限范围列表
     * @param validitySeconds 有效期(秒)
     * @return Access Token
     */
    public String generateAccessToken(String userId, String clientId, List<String> scopes, int validitySeconds) {
        // 注意：JwtUtil 的 generateToken 方法目前只接受 userId 和 email 两个参数
        // 这里暂时使用基础的 JWT 生成，将 clientId 和 scope 等信息编码到 Token 中
        // 后续可以考虑扩展 JwtUtil 支持自定义 claims

        // 目前先使用现有的 generateToken 方法，clientId 和 scope 信息暂时不编码到 JWT 中
        // 这些信息已经存储在数据库的 oauth2_authorizations 表中
        return jwtUtil.generateToken(userId, null);
    }

    /**
     * 生成 Refresh Token
     * 使用安全随机数生成 64 字节的刷新令牌
     */
    public String generateRefreshToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * 验证 Access Token 并提取用户ID
     *
     * @param accessToken Access Token
     * @return 用户ID，如果 Token 无效则返回 null
     */
    public String validateAccessToken(String accessToken) {
        try {
            return jwtUtil.getUserIdFromToken(accessToken);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 计算过期时间
     *
     * @param validitySeconds 有效期(秒)
     * @return 过期时间
     */
    public LocalDateTime calculateExpiresAt(int validitySeconds) {
        return LocalDateTime.now().plusSeconds(validitySeconds);
    }

    /**
     * 检查是否过期
     *
     * @param expiresAt 过期时间
     * @return true 如果已过期
     */
    public boolean isExpired(LocalDateTime expiresAt) {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
