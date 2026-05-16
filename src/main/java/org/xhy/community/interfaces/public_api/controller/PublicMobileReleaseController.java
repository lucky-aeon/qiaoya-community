package org.xhy.community.interfaces.public_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.mobile.dto.MobileReleaseDTO;
import org.xhy.community.application.mobile.service.MobileReleaseAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

@RestController
@RequestMapping("/api/public/mobile/releases")
public class PublicMobileReleaseController {

    private final MobileReleaseAppService mobileReleaseAppService;

    public PublicMobileReleaseController(MobileReleaseAppService mobileReleaseAppService) {
        this.mobileReleaseAppService = mobileReleaseAppService;
    }

    @GetMapping("/android/latest")
    public ApiResponse<MobileReleaseDTO> getLatestAndroidRelease() {
        return ApiResponse.success(mobileReleaseAppService.getLatestAndroidRelease());
    }
}
