package org.xhy.community.application.session.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.session.assembler.DeviceSessionAssembler;
import org.xhy.community.application.session.dto.ActiveSessionDTO;
import org.xhy.community.domain.session.service.DeviceSessionDomainService;
import org.xhy.community.domain.config.service.UserSessionConfigService;
import org.xhy.community.domain.session.valueobject.ActiveIpInfo;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.UserErrorCode;

import java.util.List;

/**
 * 设备会话应用服务（用户使用）
 */
@Service
public class DeviceSessionAppService {

    private final DeviceSessionDomainService deviceSessionDomainService;
    private final UserSessionConfigService userSessionConfigService;

    public DeviceSessionAppService(DeviceSessionDomainService deviceSessionDomainService,
                                   UserSessionConfigService userSessionConfigService) {
        this.deviceSessionDomainService = deviceSessionDomainService;
        this.userSessionConfigService = userSessionConfigService;
    }

    /**
     * 获取用户活跃会话列表
     */
    public List<ActiveSessionDTO> getUserActiveSessions(String userId, String currentIp) {
        List<ActiveIpInfo> activeIps = deviceSessionDomainService.getActiveIpsWithLastSeen(userId, currentIp);
        return DeviceSessionAssembler.toActiveSessionDTOList(activeIps);
    }

    /**
     * 用户主动下线指定IP的会话
     */
    public void removeUserActiveSession(String userId, String ip) {
        // 校验IP是否属于该用户的活跃会话
        List<ActiveIpInfo> activeIps = deviceSessionDomainService.getActiveIpsWithLastSeen(userId);
        boolean ipExists = activeIps.stream()
                .anyMatch(activeIp -> ip.equals(activeIp.getIp()));

        if (!ipExists) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND, "该IP不在您的活跃设备列表中");
        }

        deviceSessionDomainService.removeActiveIp(userId, ip);
    }

    /**
     * 校验指定用户在给定IP上的会话是否被允许（是否活跃）
     */
    public boolean isIpAllowed(String userId, String ip) {
        long ttlMs = userSessionConfigService.getUserSessionConfig().getTtl().toMillis();
        return deviceSessionDomainService.isIpActive(userId, ip, ttlMs);
    }
}
