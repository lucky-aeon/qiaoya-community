package org.xhy.community.interfaces.reaction.controller;

import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.reaction.dto.ReactionSummaryDTO;
import org.xhy.community.application.reaction.service.ReactionAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.reaction.request.ToggleReactionRequest;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reactions")
public class ReactionController {

    private final ReactionAppService reactionAppService;

    public ReactionController(ReactionAppService reactionAppService) {
        this.reactionAppService = reactionAppService;
    }

    /** 切换通用表情回复状态（需要登录） */
    @PostMapping("/toggle")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "REACTION_TOGGLE", name = "切换表情回复")})
    public ApiResponse<Map<String, Object>> toggle(@Valid @RequestBody ToggleReactionRequest request) {
        String userId = UserContext.getCurrentUserId();
        boolean added = reactionAppService.toggle(request, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("added", added);
        return ApiResponse.success(data);
    }

    /** 获取单个业务对象的表情统计 */
    @GetMapping("/{businessType}/{businessId}")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "REACTION_SUMMARY", name = "获取表情统计")})
    public ApiResponse<List<ReactionSummaryDTO>> getSummary(@PathVariable String businessType,
                                                            @PathVariable String businessId) {
        String currentUserId = UserContext.hasCurrentUser() ? UserContext.getCurrentUserId() : null;
        List<ReactionSummaryDTO> list = reactionAppService.getSummary(businessType, businessId, currentUserId);
        return ApiResponse.success(list);
    }

    /** 批量获取多个业务对象的表情统计 */
    @GetMapping("/{businessType}/batch")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "REACTION_SUMMARY_BATCH", name = "批量表情统计")})
    public ApiResponse<Map<String, List<ReactionSummaryDTO>>> getSummaryBatch(@PathVariable String businessType,
                                                                              @RequestParam("businessIds") String businessIdsCsv) {
        if (!StringUtils.hasText(businessIdsCsv)) {
            return ApiResponse.success(Collections.emptyMap());
        }
        List<String> businessIds = Arrays.stream(businessIdsCsv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        String currentUserId = UserContext.hasCurrentUser() ? UserContext.getCurrentUserId() : null;
        Map<String, List<ReactionSummaryDTO>> map = reactionAppService.getSummaryBatch(businessType, businessIds, currentUserId);
        return ApiResponse.success(map);
    }
}
