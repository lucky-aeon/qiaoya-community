package org.xhy.community.interfaces.public_api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.xhy.community.application.site.dto.CreatorAboutPageDTO;
import org.xhy.community.application.site.service.CreatorAboutPageAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;

@RestController
@RequestMapping("/api/public/site")
public class PublicSiteController {

    private final CreatorAboutPageAppService creatorAboutPageAppService;

    public PublicSiteController(CreatorAboutPageAppService creatorAboutPageAppService) {
        this.creatorAboutPageAppService = creatorAboutPageAppService;
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
}
