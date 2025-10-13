package org.xhy.community.domain.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.xhy.community.infrastructure.exception.AuthErrorCode;
import org.xhy.community.infrastructure.exception.BusinessException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * 密码重置验证码领域服务
 * 负责生成、限频和校验密码重置验证码
 */
@Service
public class PasswordResetDomainService {

    private static final String KEY_CODE_PREFIX = "auth:password:reset:code:";
    private static final String KEY_EMAIL_COUNT_PREFIX = "auth:password:reset:email:";
    private static final String KEY_IP_COUNT_PREFIX = "auth:password:reset:ip:";
    private static final String KEY_IP_BAN_PREFIX = "auth:ip:";              // 复用注册封禁Key
    private static final String KEY_IP_BAN_SET = "auth:ip:ban:set";           // 复用注册封禁集合

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration IP_BAN_TTL = Duration.ofDays(7);
    private static final int EMAIL_DAILY_LIMIT = 2;
    private static final int IP_DAILY_LIMIT = 2;

    private final StringRedisTemplate redisTemplate;
    private static final Logger log = LoggerFactory.getLogger(PasswordResetDomainService.class);

    public PasswordResetDomainService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 申请密码重置验证码，并进行限频控制
     *
     * @param email 用户邮箱
     * @param ip    请求来源IP
     * @return 生成的验证码
     */
    public String requestResetCode(String email, String ip) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(ip)) {
            throw new BusinessException(AuthErrorCode.PASSWORD_RESET_PARAM_INVALID, "邮箱或IP不能为空");
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedIp = ip.trim();

        if (Boolean.TRUE.equals(redisTemplate.hasKey(keyIpBan(normalizedIp)))) {
            log.warn("【密码重置】请求被封禁：ip={} email={}", normalizedIp, normalizedEmail);
            throw new BusinessException(AuthErrorCode.IP_BANNED, "当前IP已被封禁");
        }

        enforceEmailLimit(normalizedEmail, normalizedIp);
        enforceIpLimit(normalizedIp);

        String code = generate6DigitCode();
        redisTemplate.opsForValue().set(buildCodeKey(normalizedEmail), code, CODE_TTL);
        log.info("【密码重置】验证码已生成：email={} ip={} (不记录验证码值)", normalizedEmail, normalizedIp);
        return code;
    }

    /**
     * 校验密码重置验证码，并在成功后消费
     *
     * @param email 用户邮箱
     * @param code  验证码
     */
    public void verifyAndConsume(String email, String code) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            throw new BusinessException(AuthErrorCode.PASSWORD_RESET_PARAM_INVALID, "邮箱或验证码不能为空");
        }

        String normalizedEmail = email.trim().toLowerCase();

        String key = buildCodeKey(normalizedEmail);
        String cached = redisTemplate.opsForValue().get(key);
        if (cached == null) {
            log.warn("【密码重置】验证码无效/过期：email={}", normalizedEmail);
            throw new BusinessException(AuthErrorCode.PASSWORD_RESET_CODE_INVALID);
        }
        if (!Objects.equals(cached, code)) {
            log.warn("【密码重置】验证码不匹配：email={}", normalizedEmail);
            throw new BusinessException(AuthErrorCode.PASSWORD_RESET_CODE_MISMATCH);
        }
        redisTemplate.delete(key);
        log.info("【密码重置】验证码校验成功并消费：email={}", normalizedEmail);
    }

    private void enforceEmailLimit(String email, String ip) {
        String key = buildEmailCountKey(email);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            long ttlMillis = millisUntilEndOfDay();
            if (ttlMillis > 0) {
                redisTemplate.expire(key, Duration.ofMillis(ttlMillis));
            }
        }
        if (count != null && count > EMAIL_DAILY_LIMIT) {
            banIp(ip);
            redisTemplate.delete(key);
            log.warn("【密码重置】超出邮箱日限并封禁：email={} ip={} count={} limit={}", email, ip, count, EMAIL_DAILY_LIMIT);
            throw new BusinessException(AuthErrorCode.IP_BANNED, "密码重置次数过多，当前IP已被封禁");
        }
    }

    private void enforceIpLimit(String ip) {
        String key = buildIpCountKey(ip);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            long ttlMillis = millisUntilEndOfDay();
            if (ttlMillis > 0) {
                redisTemplate.expire(key, Duration.ofMillis(ttlMillis));
            }
        }
        if (count != null && count > IP_DAILY_LIMIT) {
            banIp(ip);
            redisTemplate.delete(key);
            log.warn("【密码重置】超出IP日限并封禁：ip={} count={} limit={}", ip, count, IP_DAILY_LIMIT);
            throw new BusinessException(AuthErrorCode.IP_BANNED, "密码重置次数过多，当前IP已被封禁");
        }
    }

    private String buildCodeKey(String email) {
        return KEY_CODE_PREFIX + email;
    }

    private String buildEmailCountKey(String email) {
        return KEY_EMAIL_COUNT_PREFIX + email + ":day:" + LocalDate.now();
    }

    private String buildIpCountKey(String ip) {
        return KEY_IP_COUNT_PREFIX + ip + ":day:" + LocalDate.now();
    }

    private String keyIpBan(String ip) {
        return KEY_IP_BAN_PREFIX + ip + ":ban";
    }

    private void banIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return;
        }
        String banKey = keyIpBan(ip);
        redisTemplate.opsForValue().set(banKey, "1", IP_BAN_TTL);
        long expireAt = System.currentTimeMillis() + IP_BAN_TTL.toMillis();
        redisTemplate.opsForZSet().add(KEY_IP_BAN_SET, ip, expireAt);
    }

    public List<IpBanInfo> listBannedIps() {
        long now = System.currentTimeMillis();
        Set<ZSetOperations.TypedTuple<String>> tuples =
            redisTemplate.opsForZSet().rangeByScoreWithScores(KEY_IP_BAN_SET, now, Double.POSITIVE_INFINITY);

        List<IpBanInfo> result = new ArrayList<>();
        if (tuples == null || tuples.isEmpty()) {
            return result;
        }

        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String ip = tuple.getValue();
            Double expireAtScore = tuple.getScore();
            if (ip == null || expireAtScore == null) {
                continue;
            }

            String banKey = keyIpBan(ip);
            Long ttlSeconds = redisTemplate.getExpire(banKey);
            if (ttlSeconds == null || ttlSeconds <= 0) {
                redisTemplate.opsForZSet().remove(KEY_IP_BAN_SET, ip);
                continue;
            }

            long expireAtMillis = now + ttlSeconds * 1000;
            LocalDateTime expireAt = LocalDateTime.ofInstant(new Date(expireAtMillis).toInstant(), ZoneId.systemDefault());
            result.add(new IpBanInfo(ip, expireAt, ttlSeconds));
        }
        return result;
    }

    public void unbanIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return;
        }
        redisTemplate.delete(keyIpBan(ip));
        redisTemplate.opsForZSet().remove(KEY_IP_BAN_SET, ip);
        redisTemplate.delete(buildIpCountKey(ip));
        log.info("【密码重置】已解除封禁：ip={}", ip);
    }

    private long millisUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay();
        return ChronoUnit.MILLIS.between(now, endOfDay);
    }

    private String generate6DigitCode() {
        Random random = new Random();
        int num = 100000 + random.nextInt(900000);
        return String.valueOf(num);
    }

    public static class IpBanInfo {
        private final String ip;
        private final LocalDateTime expireAt;
        private final long remainSeconds;

        public IpBanInfo(String ip, LocalDateTime expireAt, long remainSeconds) {
            this.ip = ip;
            this.expireAt = expireAt;
            this.remainSeconds = remainSeconds;
        }

        public String getIp() {
            return ip;
        }

        public LocalDateTime getExpireAt() {
            return expireAt;
        }

        public long getRemainSeconds() {
            return remainSeconds;
        }
    }
}
