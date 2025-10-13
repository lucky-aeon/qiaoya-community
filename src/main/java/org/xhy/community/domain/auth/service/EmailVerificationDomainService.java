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
import java.util.*;

/**
 * 邮箱注册验证码与IP频控/封禁领域服务。
 * - 生成并缓存邮箱验证码（TTL 5分钟）。
 * - 按IP进行每天最多3次请求的频控，超过后封禁7天。
 * - 提供验证码校验与消费（一次性使用，使用后删除）。
 * - 维护被封禁IP集合以供管理员查看。
 */
@Service
public class EmailVerificationDomainService {

    private final StringRedisTemplate redis;
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationDomainService.class);

    private static final String KEY_EMAIL_CODE_PREFIX = "auth:email:invite:"; // auth:email:invite:{email}
    private static final String KEY_IP_DAILY_PREFIX = "auth:ip:";             // auth:ip:{ip}:day:{yyyyMMdd}:count
    private static final String KEY_IP_BAN_PREFIX = "auth:ip:";               // auth:ip:{ip}:ban
    private static final String KEY_IP_BAN_SET = "auth:ip:ban:set";           // ZSET(ip -> expireAtMillis)

    private static final int DAILY_LIMIT = 3;
    private static final Duration EMAIL_CODE_TTL = Duration.ofMinutes(5);
    private static final Duration IP_BAN_TTL = Duration.ofDays(7);

    public EmailVerificationDomainService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    private String keyEmailCode(String email) {
        return KEY_EMAIL_CODE_PREFIX + email;
    }

    private String keyIpDaily(String ip) {
        String day = LocalDate.now().toString().replace("-", "");
        return KEY_IP_DAILY_PREFIX + ip + ":day:" + day + ":count";
    }

    private String keyIpBan(String ip) {
        return KEY_IP_BAN_PREFIX + ip + ":ban";
    }

    private long millisUntilEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().plusDays(1).atStartOfDay();
        return ChronoUnit.MILLIS.between(now, endOfDay);
    }

    /**
     * 请求发送邮箱邀请码（生成并缓存代码）。
     * 会进行IP频控与封禁检查；若封禁则抛出业务异常。
     *
     * @return 生成的验证码（6位数字）
     */
    public String requestEmailInviteCode(String email, String ip) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(ip)) {
            throw new BusinessException(AuthErrorCode.IP_RATE_LIMIT_EXCEEDED, "参数无效");
        }

        // 1) IP是否已封禁
        if (Boolean.TRUE.equals(redis.hasKey(keyIpBan(ip)))) {
            log.warn("【注册验证码】请求被封禁：ip={} email={}", ip, email);
            throw new BusinessException(AuthErrorCode.IP_BANNED, "当前IP已被封禁");
        }

        // 2) 频控：同一IP当日最多3次
        String countKey = keyIpDaily(ip);
        Long newCount = redis.opsForValue().increment(countKey);
        if (newCount != null && newCount == 1L) {
            // 第一次设置当日过期（到当天24:00）
            long ttlMs = millisUntilEndOfDay();
            if (ttlMs > 0) {
                redis.expire(countKey, Duration.ofMillis(ttlMs));
            }
        }
        if (newCount != null && newCount > DAILY_LIMIT) {
            // 超限 -> 封禁7天
            String banKey = keyIpBan(ip);
            redis.opsForValue().set(banKey, "1", IP_BAN_TTL);
            long expireAt = System.currentTimeMillis() + IP_BAN_TTL.toMillis();
            redis.opsForZSet().add(KEY_IP_BAN_SET, ip, expireAt);
            log.warn("【注册验证码】触发封禁：ip={} email={} 次数={} 限制={}", ip, email, newCount, DAILY_LIMIT);
            throw new BusinessException(AuthErrorCode.IP_BANNED, "当前IP请求过多，已封禁7天");
        }

        // 3) 生成验证码并缓存（5分钟）
        String code = generate6DigitCode();
        redis.opsForValue().set(keyEmailCode(email), code, EMAIL_CODE_TTL);
        log.info("【注册验证码】已生成：email={} ip={} (不记录验证码值)", email, ip);
        return code;
    }

    /**
     * 校验邮箱邀请码并在成功后删除。
     */
    public void verifyAndConsume(String email, String code) {
        String key = keyEmailCode(email);
        String cached = redis.opsForValue().get(key);
        if (cached == null) {
            log.warn("【注册验证码】无效/过期：email={}", email);
            throw new BusinessException(AuthErrorCode.EMAIL_CODE_INVALID);
        }
        if (!Objects.equals(cached, code)) {
            log.warn("【注册验证码】校验失败：email={} 提供的验证码不匹配", email);
            throw new BusinessException(AuthErrorCode.EMAIL_CODE_MISMATCH);
        }
        // 一次性：使用后删除
        redis.delete(key);
        log.info("【注册验证码】校验成功并消费：email={}", email);
    }

    /**
     * 列出仍处于封禁期的IP。
     * 将自动清理已过期的成员。
     */
    public List<IpBanInfo> listBannedIps() {
        long now = System.currentTimeMillis();
        Set<ZSetOperations.TypedTuple<String>> tuples =
            redis.opsForZSet().rangeByScoreWithScores(KEY_IP_BAN_SET, now, Double.POSITIVE_INFINITY);

        List<IpBanInfo> result = new ArrayList<>();
        if (tuples == null || tuples.isEmpty()) {
            return result;
        }

        for (ZSetOperations.TypedTuple<String> t : tuples) {
            String ip = t.getValue();
            Double expireAtScore = t.getScore();
            if (ip == null || expireAtScore == null) continue;

            String banKey = keyIpBan(ip);
            Long ttlSeconds = redis.getExpire(banKey);
            if (ttlSeconds == null || ttlSeconds <= 0) {
                // 已过期：从集合中清理
                redis.opsForZSet().remove(KEY_IP_BAN_SET, ip);
                continue;
            }
            long expireAtMillis = now + ttlSeconds * 1000;
            LocalDateTime expireAt = LocalDateTime.ofInstant(
                new Date(expireAtMillis).toInstant(), ZoneId.systemDefault());
            result.add(new IpBanInfo(ip, expireAt, ttlSeconds));
        }
        return result;
    }

    /**
     * 解除指定IP封禁，并重置当日计数。
     */
    public void unbanIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return;
        }
        // 移除封禁标记
        redis.delete(keyIpBan(ip));
        // 从被封禁集合中移除
        redis.opsForZSet().remove(KEY_IP_BAN_SET, ip);
        // 重置当日计数，避免立刻再次封禁
        redis.delete(keyIpDaily(ip));
        log.info("【注册验证码】已解除封禁：ip={}", ip);
    }

    private String generate6DigitCode() {
        Random r = new Random();
        int num = 100000 + r.nextInt(900000);
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

        public String getIp() { return ip; }
        public LocalDateTime getExpireAt() { return expireAt; }
        public long getRemainSeconds() { return remainSeconds; }
    }
}
