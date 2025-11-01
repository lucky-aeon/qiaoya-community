package org.xhy.community.application.security.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.security.dto.BannedIpDTO;
import org.xhy.community.domain.auth.service.EmailVerificationDomainService;
import org.xhy.community.interfaces.user.request.BanIpRequest;

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

    public void unbanIp(String ip) {
        emailVerificationDomainService.unbanIp(ip);
    }

    public void banIp(BanIpRequest request) {
        // API层已做格式校验，这里直接编排调用
        long days = request.getTtlDays() != null ? request.getTtlDays() : 7L;
        if (days < 1) {
            days = 1L;
        }
        emailVerificationDomainService.banIp(request.getIp(), days);
    }
}
