package org.xhy.community.interfaces.ainews.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.ainews.dto.AdminDailyItemDTO;
import org.xhy.community.application.ainews.service.AdminDailyAppService;
import org.xhy.community.application.ainews.service.AibaseIngestAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.ainews.request.AdminDailyQueryRequest;

@RestController
@RequestMapping("/api/admin/ai-news")
public class AdminDailyNewsController {

    private final AdminDailyAppService adminDailyAppService;

    public AdminDailyNewsController(AdminDailyAppService adminDailyAppService) {
        this.adminDailyAppService = adminDailyAppService;
    }

    @GetMapping
    public ApiResponse<IPage<AdminDailyItemDTO>> page(@Valid AdminDailyQueryRequest request) {
        return ApiResponse.success(adminDailyAppService.pageDailyItems(request));
    }

    @PostMapping("/ingest")
    public ApiResponse<AibaseIngestAppService.IngestResult> ingest() {
        return ApiResponse.success(adminDailyAppService.manualIngest());
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publish(@PathVariable("id") String id) {
        adminDailyAppService.publish(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/hide")
    public ApiResponse<Void> hide(@PathVariable("id") String id) {
        adminDailyAppService.hide(id);
        return ApiResponse.success();
    }
}
