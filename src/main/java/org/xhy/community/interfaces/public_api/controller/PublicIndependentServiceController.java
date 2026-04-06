package org.xhy.community.interfaces.public_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xhy.community.application.config.dto.IndependentServiceDTO;
import org.xhy.community.application.config.service.IndependentServiceAppService;
import org.xhy.community.infrastructure.config.ApiResponse;

import java.util.List;

/**
 * 对外独立服务控制器
 */
@RestController
@RequestMapping("/api/public/independent-services")
public class PublicIndependentServiceController {

    private final IndependentServiceAppService independentServiceAppService;

    public PublicIndependentServiceController(IndependentServiceAppService independentServiceAppService) {
        this.independentServiceAppService = independentServiceAppService;
    }

    @GetMapping
    public ApiResponse<List<IndependentServiceDTO>> listIndependentServices() {
        return ApiResponse.success(independentServiceAppService.listPublicServices());
    }

    @GetMapping("/{serviceCode}")
    public ApiResponse<IndependentServiceDTO> getIndependentService(@PathVariable String serviceCode) {
        return ApiResponse.success(independentServiceAppService.getPublicService(serviceCode));
    }
}
