package org.xhy.community.interfaces.public_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.xhy.community.application.site.dto.CreatorAboutPageDTO;
import org.xhy.community.application.site.service.CreatorAboutPageAppService;
import org.xhy.community.domain.config.valueobject.PlusGuideConfig;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;

@RestController
@RequestMapping("/api/public/site")
public class PublicSiteController {

    private final CreatorAboutPageAppService creatorAboutPageAppService;
    private final SystemConfigDomainService systemConfigDomainService;

    public PublicSiteController(CreatorAboutPageAppService creatorAboutPageAppService,
                                SystemConfigDomainService systemConfigDomainService) {
        this.creatorAboutPageAppService = creatorAboutPageAppService;
        this.systemConfigDomainService = systemConfigDomainService;
    }

    @GetMapping("/about")
    public ApiResponse<CreatorAboutPageDTO> getCreatorAboutPage() {
        try {
            return ApiResponse.success(creatorAboutPageAppService.getPublicAboutPage());
        } catch (BusinessException ex) {
            if (ex.getCode() == SystemConfigErrorCode.CONFIG_NOT_FOUND.getCode()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
            }
            throw ex;
        }
    }

    @GetMapping("/plus-guide-config")
    public ApiResponse<PlusGuideConfig> getPlusGuideConfig() {
        PlusGuideConfig config = systemConfigDomainService.getConfigData(
            SystemConfigType.PLUS_GUIDE, PlusGuideConfig.class);
        return ApiResponse.success(config != null ? config : new PlusGuideConfig());
    }
}
