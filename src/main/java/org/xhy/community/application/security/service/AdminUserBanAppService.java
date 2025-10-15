package org.xhy.community.application.security.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.security.dto.BannedUserDTO;
import org.xhy.community.domain.session.service.DeviceSessionDomainService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserBanAppService {

    private final DeviceSessionDomainService deviceSessionDomainService;

    public AdminUserBanAppService(DeviceSessionDomainService deviceSessionDomainService) {
        this.deviceSessionDomainService = deviceSessionDomainService;
    }

    public List<BannedUserDTO> listBannedUsers() {
        return deviceSessionDomainService.listBannedUsers().stream()
                .map(info -> new BannedUserDTO(info.getUserId(), info.getExpireAt(), info.getRemainSeconds()))
                .collect(Collectors.toList());
    }

    public void unbanUser(String userId) {
        deviceSessionDomainService.unbanUser(userId);
    }
}

