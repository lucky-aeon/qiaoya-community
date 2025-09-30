package org.xhy.community.interfaces.ainews.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.ainews.dto.DailyItemDTO;
import org.xhy.community.application.ainews.dto.HistoryDateDTO;
import org.xhy.community.application.ainews.service.DailyAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.ainews.request.DailyQueryRequest;

import java.util.List;

@RestController
@RequestMapping("/api/app/ai-news")
public class AppDailyNewsController {

    private final DailyAppService dailyAppService;

    public AppDailyNewsController(DailyAppService dailyAppService) {
        this.dailyAppService = dailyAppService;
    }

    @GetMapping("/dates")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "AI_NEWS_APP_DATES", name = "查看AI日报日期")})
    public ApiResponse<List<HistoryDateDTO>> getHistoryDates() {
        return ApiResponse.success(dailyAppService.listHistoryDates());
    }

    @GetMapping("/daily")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "AI_NEWS_APP_LIST", name = "查看AI日报列表")})
    public ApiResponse<IPage<DailyItemDTO>> getDaily(@Valid DailyQueryRequest request) {
        String date = request.getDate();
        if (date == null || date.isBlank()) {
            date = dailyAppService.getLatestDate();
        }
        IPage<DailyItemDTO> page = dailyAppService.pageDailyItems(date, request.getPageNum(), request.getPageSize(), Boolean.TRUE.equals(request.getWithContent()));
        return ApiResponse.success(page);
    }

    @GetMapping("/detail/{id}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "AI_NEWS_APP_DETAIL", name = "查看AI日报详情")})
    public ApiResponse<DailyItemDTO> getDetail(@PathVariable("id") String id) {
        return ApiResponse.success(dailyAppService.getById(id));
        
    }
}

