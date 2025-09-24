package org.xhy.community.application.user.service;

import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.xhy.community.application.user.assembler.UserAssembler;
import org.xhy.community.application.user.dto.LoginResponseDTO;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.dto.UserPublicProfileDTO;
import org.xhy.community.application.user.dto.UserStatsDTO;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.infrastructure.exception.UserErrorCode;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.config.JwtUtil;
import org.xhy.community.domain.session.service.DeviceSessionDomainService;
import org.xhy.community.domain.session.service.TokenIpMappingDomainService;
import org.xhy.community.domain.config.service.UserSessionConfigService;
import org.xhy.community.domain.config.valueobject.UserSessionConfig;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.application.subscription.assembler.UserSubscriptionAssembler;
import org.xhy.community.application.subscription.dto.UserSubscriptionDTO;
import org.xhy.community.interfaces.user.request.UpdateProfileRequest;
import org.xhy.community.domain.auth.service.EmailVerificationDomainService;
import org.xhy.community.infrastructure.email.EmailService;
import org.xhy.community.infrastructure.exception.AuthErrorCode;
import org.xhy.community.domain.user.event.UserLoginEvent;

@Service
public class UserAppService {
    
    private final UserDomainService userDomainService;
    private final JwtUtil jwtUtil;
    private final DeviceSessionDomainService deviceSessionDomainService;
    private final UserSessionConfigService userSessionConfigService;
    private final TokenIpMappingDomainService tokenIpMappingDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final EmailVerificationDomainService emailVerificationDomainService;
    private final EmailService emailService;
    private final ApplicationEventPublisher eventPublisher;

    public UserAppService(UserDomainService userDomainService,
                          JwtUtil jwtUtil,
                          DeviceSessionDomainService deviceSessionDomainService,
                          UserSessionConfigService userSessionConfigService,
                          TokenIpMappingDomainService tokenIpMappingDomainService,
                          SubscriptionDomainService subscriptionDomainService,
                          SubscriptionPlanDomainService subscriptionPlanDomainService,
                          EmailVerificationDomainService emailVerificationDomainService,
                          EmailService emailService,
                          ApplicationEventPublisher eventPublisher) {
        this.userDomainService = userDomainService;
        this.jwtUtil = jwtUtil;
        this.deviceSessionDomainService = deviceSessionDomainService;
        this.userSessionConfigService = userSessionConfigService;
        this.tokenIpMappingDomainService = tokenIpMappingDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.emailVerificationDomainService = emailVerificationDomainService;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
    }
    
    public LoginResponseDTO login(String email, String password, String ip) {
        if (!userDomainService.authenticateUser(email, password)) {
            throw new BusinessException(UserErrorCode.WRONG_PASSWORD, "邮箱或密码错误");
        }

        UserEntity user = userDomainService.getUserByEmail(email);

        // 获取用户会话配置
        UserSessionConfig sessionConfig = userSessionConfigService.getUserSessionConfig();

        // 设备/IP 并发控制：基于 IP 的会话限制
        boolean allowed = deviceSessionDomainService.createOrReuseByIp(
                user.getId(), ip,
                sessionConfig.getMaxActiveIps(), sessionConfig.getPolicy(),
                sessionConfig.getTtl().toMillis(), sessionConfig.getHistoryWindow().toMillis(),
                sessionConfig.getBanThreshold(), sessionConfig.getBanTtl().toMillis());
        if (!allowed) {
            throw new BusinessException(UserErrorCode.USER_BANNED, "设备或IP限制，登录被拒绝");
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        // 建立token和IP的映射关系，用于后续设备下线时能找到对应token
        tokenIpMappingDomainService.mapTokenToIp(user.getId(), ip, token, sessionConfig.getTtl());

        // 发布用户登录事件
        eventPublisher.publishEvent(new UserLoginEvent(this, user.getId(), user.getEmail(), ip));

        return new LoginResponseDTO(token, this.getCurrentUserInfo(user.getId()));
    }
    
    public UserDTO register(String email, String emailVerificationCode, String password) {
        if (userDomainService.isEmailExists(email, null)) {
            throw new BusinessException(UserErrorCode.EMAIL_EXISTS);
        }
        // 验证邮箱邀请码（通过后自动删除）
        emailVerificationDomainService.verifyAndConsume(email, emailVerificationCode);

        UserEntity user = userDomainService.registerUser(email, password);
        return UserAssembler.toDTO(user);
    }

    /**
     * 发送注册邮箱邀请码（带IP频控与封禁）。
     */
    public void sendRegisterEmailCode(String email, String ip) {
        // 如果邮箱已存在，直接报错（避免骚扰）
        if (userDomainService.isEmailExists(email, null)) {
            throw new BusinessException(UserErrorCode.EMAIL_EXISTS);
        }

        // 生成并缓存邀请码（含IP限频/封禁逻辑）
        String code = emailVerificationDomainService.requestEmailInviteCode(email, ip);

        // 发送邮件（基础设施层）
        String subject = "【敲鸭社区】注册邀请码";
        String content = "<p>您的注册邀请码为：<b>" + code + "</b></p>" +
                "<p>有效期5分钟，请勿泄露。</p>";
        boolean sent = emailService.sendEmail(email, subject, content);
        if (!sent) {
            throw new BusinessException(AuthErrorCode.EMAIL_SEND_FAILED, "邮件发送失败，请稍后再试");
        }
    }
    
    public UserDTO updateProfile(String userId, UpdateProfileRequest request) {
        // 统一更新路径：Request -> Assembler -> Entity -> Domain.update(entity)
        UserEntity patch = UserAssembler.fromUpdateProfileRequest(request, userId);
        UserEntity user = userDomainService.updateUserProfile(patch);
        return UserAssembler.toDTO(user);
    }
    
    public UserDTO changePassword(String userId, String oldPassword, String newPassword) {
        UserEntity user = userDomainService.changeUserPassword(userId, oldPassword, newPassword);
        return UserAssembler.toDTO(user);
    }
    
    public UserDTO updateEmailNotification(String userId, Boolean emailNotificationEnabled) {
        UserEntity user = userDomainService.updateUserSettings(userId, emailNotificationEnabled, null);
        return UserAssembler.toDTO(user);
    }
    
    public UserDTO toggleEmailNotification(String userId) {
        UserEntity currentUser = userDomainService.getUserById(userId);
        Boolean currentSetting = currentUser.getEmailNotificationEnabled();
        Boolean newSetting = !Boolean.TRUE.equals(currentSetting);
        
        UserEntity user = userDomainService.updateUserSettings(userId, newSetting, null);
        return UserAssembler.toDTO(user);
    }
    
    public UserDTO getCurrentUserInfo(String userId) {
        UserEntity user = userDomainService.getUserById(userId);
        UserDTO dto = UserAssembler.toDTO(user);

        // 附加当前有效套餐信息（若存在）
        var actives = subscriptionDomainService.getUserActiveSubscriptions(userId);
        if (actives != null && !actives.isEmpty()) {
            // 取最新创建的订阅作为当前套餐
            UserSubscriptionEntity current = actives.get(0);
            String planName = subscriptionPlanDomainService
                    .getSubscriptionPlanById(current.getSubscriptionPlanId())
                    .getName();
            dto.setCurrentSubscriptionPlanId(current.getSubscriptionPlanId());
            dto.setCurrentSubscriptionPlanName(planName);
            dto.setCurrentSubscriptionStartTime(current.getStartTime());
            dto.setCurrentSubscriptionEndTime(current.getEndTime());
        }

        return dto;
    }
    
    public UserPublicProfileDTO getUserPublicProfile(String userId) {
        UserEntity user = userDomainService.getUserById(userId);
        return UserAssembler.toPublicProfileDTO(user);
    }

    /**
     * 判断用户是否处于激活状态
     */
    public boolean isUserActive(String userId) {
        try {
            UserEntity user = userDomainService.getUserById(userId);
            return user != null && user.isActive();
        } catch (Exception e) {
            // 如果用户不存在或查询失败，返回false
            return false;
        }
    }

    /**
     * 判断用户是否管理员
     * 供接口层拦截器使用，避免直接依赖领域服务。
     */
    public boolean isAdmin(String userId) {
        UserEntity user = userDomainService.getUserById(userId);
        return user != null && user.isAdmin();
    }

    /**
     * 获取用户统计信息
     *
     * @return 用户统计DTO
     */
    public UserStatsDTO getUserStats() {
        long totalCount = userDomainService.getTotalUserCount();
        return new UserStatsDTO(totalCount);
    }
}
