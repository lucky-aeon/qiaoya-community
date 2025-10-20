package org.xhy.community.interfaces.read.controller;

import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.read.dto.UnreadSummaryDTO;
import org.xhy.community.application.read.service.UnreadAppService;
import org.xhy.community.domain.common.valueobject.ReadChannel;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;

/**
 * 用户未读汇总（文章/题目）控制器
 * 仅用于导航栏小红点展示与清零
 */
@RestController
@RequestMapping("/api/user/unread")
public class UserUnreadController {

    private final UnreadAppService unreadAppService;

    public UserUnreadController(UnreadAppService unreadAppService) {
        this.unreadAppService = unreadAppService;
    }

    /** 获取用户的未读汇总（文章/题目） */
    @GetMapping("/summary")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "UNREAD_SUMMARY", name = "未读汇总")})
    public ApiResponse<UnreadSummaryDTO> getUnreadSummary() {
        String userId = UserContext.getCurrentUserId();
        UnreadSummaryDTO dto = unreadAppService.getUnreadSummary(userId);
        return ApiResponse.success(dto);
    }

    /** 进入频道列表后清零（更新 Last Seen） */
    @PutMapping("/visit")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "UNREAD_VISIT", name = "未读清零")})
    public ApiResponse<Void> visitChannel(@RequestParam("channel") ReadChannel channel) {
        String userId = UserContext.getCurrentUserId();
        unreadAppService.visitChannel(userId, channel);
        return ApiResponse.success();
    }
}

