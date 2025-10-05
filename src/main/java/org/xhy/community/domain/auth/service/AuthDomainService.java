package org.xhy.community.domain.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Duration;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.auth.entity.UserSocialAccountEntity;
import org.xhy.community.domain.auth.repository.UserSocialAccountRepository;
import org.xhy.community.domain.auth.query.UserSocialAccountQuery;
import org.xhy.community.domain.auth.valueobject.OpenIdProfile;
import org.xhy.community.domain.common.valueobject.AuthProvider;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.repository.UserRepository;
import org.xhy.community.infrastructure.exception.AuthErrorCode;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.domain.log.service.UserActivityLogDomainService;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.infrastructure.util.HttpRequestInfoExtractor;
import org.xhy.community.infrastructure.context.UserActivityContext;

import java.util.Random;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@Service
public class AuthDomainService {

    private final UserSocialAccountRepository userSocialAccountRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserActivityLogDomainService userActivityLogDomainService;
    private final org.xhy.community.infrastructure.lock.DistributedLock distributedLock;

    public AuthDomainService(UserSocialAccountRepository userSocialAccountRepository,
                             UserRepository userRepository,
                             BCryptPasswordEncoder passwordEncoder,
                             UserActivityLogDomainService userActivityLogDomainService,
                             org.xhy.community.infrastructure.lock.DistributedLock distributedLock) {
        this.userSocialAccountRepository = userSocialAccountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userActivityLogDomainService = userActivityLogDomainService;
        this.distributedLock = distributedLock;
    }

    public UserEntity getOrCreateUserByGithub(OpenIdProfile profile) {
        if (profile == null || profile.getProvider() != AuthProvider.GITHUB) {
            throw new IllegalArgumentException("profile or provider invalid");
        }

        String openId = profile.getOpenId();
        String lockKey = "lock:oauth:github:openid:" + openId;
        return distributedLock.executeWithLock(lockKey, Duration.ofMillis(300), Duration.ofSeconds(5), () -> {
            // 1) 先按 provider+openId 查绑定
            UserSocialAccountEntity existing = userSocialAccountRepository.selectOne(
                new LambdaQueryWrapper<UserSocialAccountEntity>()
                    .eq(UserSocialAccountEntity::getProvider, profile.getProvider())
                    .eq(UserSocialAccountEntity::getOpenId, openId)
            );
            if (existing != null) {
                return userRepository.selectById(existing.getUserId());
            }

            // 2) 邮箱合并（允许且可用）
            if (profile.isAllowMergeByEmail() && StringUtils.hasText(profile.getEmail())) {
                String normalizedEmail = profile.getEmail().trim().toLowerCase();
                UserEntity byEmail = userRepository.selectOne(
                    new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getEmail, normalizedEmail)
                );
                if (byEmail != null) {
                    // 绑定到该用户
                    bindInternal(byEmail.getId(), profile);
                    // 记录邮箱合并审计
                    try {
                        UserActivityContext ctx = HttpRequestInfoExtractor.extractUserActivityContext();
                        userActivityLogDomainService.recordActivity(
                            byEmail.getId(), ActivityType.OAUTH_EMAIL_MERGE,
                            ctx.getBrowser(), ctx.getEquipment(), ctx.getIp(), ctx.getUserAgent(), null
                        );
                    } catch (Exception ignored) {}
                    // 可选：仅当站内为空才补全头像/昵称
                    return byEmail;
                }
            }

            // 3) 创建新用户并绑定（若邮箱已被占用且不允许合并，则报冲突）
            String email = StringUtils.hasText(profile.getEmail()) ? profile.getEmail().trim().toLowerCase() : null;
            if (email == null) {
                // 站内用户表邮箱非空约束，且业务需要邮箱唯一性
                throw new BusinessException(AuthErrorCode.OAUTH_EMAIL_REQUIRED);
            }
            UserEntity existed = userRepository.selectOne(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getEmail, email)
            );
            if (existed != null) {
                throw new BusinessException(AuthErrorCode.OAUTH_BIND_CONFLICT, "邮箱已存在，无法自动合并");
            }
            String nickname = StringUtils.hasText(profile.getName()) ? profile.getName() :
                    (StringUtils.hasText(profile.getLogin()) ? profile.getLogin() : generateDefaultNickname());
            String randomPassword = generateRandomPassword();
            String encrypted = passwordEncoder.encode(randomPassword);

            UserEntity user = new UserEntity(nickname, email, encrypted);
            userRepository.insert(user);

            bindInternal(user.getId(), profile);
            return user;
        });
    }

    public void bindGithub(String userId, OpenIdProfile profile) {
        if (profile == null || profile.getProvider() != AuthProvider.GITHUB) {
            throw new IllegalArgumentException("profile or provider invalid");
        }
        String lockKey = "lock:oauth:github:openid:" + profile.getOpenId();
        distributedLock.runWithLock(lockKey, Duration.ofMillis(300), Duration.ofSeconds(5), () -> {
            // (provider, openId) 不可被其他用户占用
            UserSocialAccountEntity existing = userSocialAccountRepository.selectOne(
                new LambdaQueryWrapper<UserSocialAccountEntity>()
                    .eq(UserSocialAccountEntity::getProvider, profile.getProvider())
                    .eq(UserSocialAccountEntity::getOpenId, profile.getOpenId())
            );
            if (existing != null && !existing.getUserId().equals(userId)) {
                throw new BusinessException(AuthErrorCode.OAUTH_ALREADY_BOUND);
            }
            if (existing == null) {
                bindInternal(userId, profile);
            }
        });
    }

    public void unbindGithubByUserId(String userId) {
        // 软删由 MyBatis-Plus @TableLogic 处理，这里使用 delete 条件删除
        userSocialAccountRepository.delete(new LambdaQueryWrapper<UserSocialAccountEntity>()
            .eq(UserSocialAccountEntity::getUserId, userId)
            .eq(UserSocialAccountEntity::getProvider, AuthProvider.GITHUB)
        );
    }

    public void unbindById(String bindId) {
        userSocialAccountRepository.deleteById(bindId);
    }

    public UserSocialAccountEntity getBindingById(String id) {
        return userSocialAccountRepository.selectById(id);
    }

    public UserSocialAccountEntity getGithubBindingByUserId(String userId) {
        return userSocialAccountRepository.selectOne(new LambdaQueryWrapper<UserSocialAccountEntity>()
            .eq(UserSocialAccountEntity::getUserId, userId)
            .eq(UserSocialAccountEntity::getProvider, AuthProvider.GITHUB)
        );
    }

    private void bindInternal(String userId, OpenIdProfile profile) {
        UserSocialAccountEntity entity = new UserSocialAccountEntity();
        entity.setUserId(userId);
        entity.setProvider(profile.getProvider());
        entity.setOpenId(profile.getOpenId());
        entity.setLogin(profile.getLogin());
        entity.setAvatarUrl(profile.getAvatarUrl());
        userSocialAccountRepository.insert(entity);
    }

    public IPage<UserSocialAccountEntity> pageSocialAccounts(UserSocialAccountQuery query) {
        Page<UserSocialAccountEntity> page = new Page<>(query.getPageNum(), query.getPageSize());
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserSocialAccountEntity> qw =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserSocialAccountEntity>()
                        .eq(StringUtils.hasText(query.getUserId()), UserSocialAccountEntity::getUserId, query.getUserId())
                        .eq(query.getProvider() != null, UserSocialAccountEntity::getProvider, query.getProvider())
                        .like(StringUtils.hasText(query.getLogin()), UserSocialAccountEntity::getLogin, query.getLogin())
                        .ge(query.getStartTime() != null, UserSocialAccountEntity::getCreateTime, query.getStartTime())
                        .le(query.getEndTime() != null, UserSocialAccountEntity::getCreateTime, query.getEndTime())
                        .orderByDesc(UserSocialAccountEntity::getCreateTime);
        return userSocialAccountRepository.selectPage(page, qw);
    }

    private String generateDefaultNickname() {
        Random random = new Random();
        int randomNumber = 100000 + random.nextInt(900000);
        return "敲鸭-" + randomNumber;
    }

    private String generateRandomPassword() {
        // 简单随机密码，实际可增强
        return java.util.UUID.randomUUID().toString();
    }
}
