package org.xhy.community.application.user.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.user.assembler.UserAssembler;
import org.xhy.community.application.user.dto.LoginResponseDTO;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.application.user.dto.UserPublicProfileDTO;
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

@Service
public class UserAppService {
    
    private final UserDomainService userDomainService;
    private final JwtUtil jwtUtil;
    private final DeviceSessionDomainService deviceSessionDomainService;
    private final UserSessionConfigService userSessionConfigService;
    private final TokenIpMappingDomainService tokenIpMappingDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;

    public UserAppService(UserDomainService userDomainService,
                          JwtUtil jwtUtil,
                          DeviceSessionDomainService deviceSessionDomainService,
                          UserSessionConfigService userSessionConfigService,
                          TokenIpMappingDomainService tokenIpMappingDomainService,
                          SubscriptionDomainService subscriptionDomainService,
                          SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.userDomainService = userDomainService;
        this.jwtUtil = jwtUtil;
        this.deviceSessionDomainService = deviceSessionDomainService;
        this.userSessionConfigService = userSessionConfigService;
        this.tokenIpMappingDomainService = tokenIpMappingDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }
    
    public LoginResponseDTO login(String email, String password, String ip) {
        if (!userDomainService.authenticateUser(email, password)) {
            throw new BusinessException(UserErrorCode.WRONG_PASSWORD, "邮箱或密码错误");
        }

        UserEntity user = userDomainService.getUserByEmail(email);
        UserDTO userDTO = UserAssembler.toDTO(user);

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

        return new LoginResponseDTO(token, userDTO);
    }
    
    public UserDTO register(String email, String emailVerificationCode, String password) {
        if (userDomainService.isEmailExists(email, null)) {
            throw new BusinessException(UserErrorCode.EMAIL_EXISTS);
        }
        
        UserEntity user = userDomainService.registerUser(email, password);
        return UserAssembler.toDTO(user);
    }
    
    public UserDTO updateProfile(String userId, String description) {
        UserEntity user = userDomainService.updateUserProfile(userId, null, description, null);
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
            UserSubscriptionDTO currentDto = UserSubscriptionAssembler.toDTOWithPlanName(current, planName);

            dto.setCurrentSubscription(currentDto);
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
}
