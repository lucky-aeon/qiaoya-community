package org.xhy.community.interfaces.ainews.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.ainews.dto.DailyItemDTO;
import org.xhy.community.application.ainews.dto.TodayDailyDTO;
import org.xhy.community.application.ainews.dto.HistoryOverviewDTO;
import org.xhy.community.application.ainews.service.DailyAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.interfaces.ainews.request.DailyQueryRequest;
import org.xhy.community.interfaces.common.request.PageRequest;

@RestController
@RequestMapping("/api/app/ai-news")
public class AppDailyNewsController {

    private final DailyAppService dailyAppService;

    public AppDailyNewsController(DailyAppService dailyAppService) {
        this.dailyAppService = dailyAppService;
    }

    // 1) 查看当天 AI 日报：返回列表（不含详情）+ 当天标题列表
    @GetMapping("/today")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "AI_NEWS_APP_TODAY", name = "查看当天AI日报")})
    public ApiResponse<TodayDailyDTO> getToday(@Valid DailyQueryRequest request) {
        // 不允许返回详情
        request.setWithContent(false);
        TodayDailyDTO dto = dailyAppService.getTodayDaily(request);
        return ApiResponse.success(dto);
    }

    // 2) 查看往期 AI 日报：分页返回（大标题 + 数量）
    @GetMapping("/history")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "AI_NEWS_APP_HISTORY", name = "查看往期AI日报")})
    public ApiResponse<IPage<HistoryOverviewDTO>> pageHistory(@Valid PageRequest request) {
        IPage<HistoryOverviewDTO> page = dailyAppService.pageHistoryOverview(request);
        return ApiResponse.success(page);
    }

    // 3) 根据日期查询日报列表：全部字段返回（含详情）
    @GetMapping("/daily")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "AI_NEWS_APP_BY_DATE", name = "根据日期查看AI日报")})
    public ApiResponse<IPage<DailyItemDTO>> getDailyByDate(@Valid DailyQueryRequest request) {
        String date = request.getDate();
        if (date == null || date.isBlank()) {
            return ApiResponse.error(400, "参数 date 必填，格式为 YYYY-MM-DD");
        }
        request.setWithContent(true);
        IPage<DailyItemDTO> page = dailyAppService.pageDailyItems(request);
        return ApiResponse.success(page);
    }
}
