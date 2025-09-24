package org.xhy.community.application.security.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.security.dto.BannedIpDTO;
import org.xhy.community.domain.auth.service.EmailVerificationDomainService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminIpAppService {

    private final EmailVerificationDomainService emailVerificationDomainService;

    public AdminIpAppService(EmailVerificationDomainService emailVerificationDomainService) {
        this.emailVerificationDomainService = emailVerificationDomainService;
    }

    public List<BannedIpDTO> listBannedIps() {
        return emailVerificationDomainService.listBannedIps().stream()
                .map(info -> new BannedIpDTO(info.getIp(), info.getExpireAt(), info.getRemainSeconds()))
                .collect(Collectors.toList());
    }
}

