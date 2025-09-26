package org.xhy.community.interfaces.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.notification.dto.NotificationDTO;
import org.xhy.community.application.notification.service.NotificationAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.notification.request.NotificationQueryRequest;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

/**
 * 用户消息中心控制器
 * 提供用户消息管理功能，包括消息列表查看、已读状态管理、未读数量统计等
 * @module 消息中心
 */
@RestController
@RequestMapping("/api/user/notifications")
public class UserNotificationController {

    private final NotificationAppService notificationAppService;

    public UserNotificationController(NotificationAppService notificationAppService) {
        this.notificationAppService = notificationAppService;
    }

    /**
     * 获取用户消息列表
     * 分页查询当前登录用户的站内消息列表，按创建时间倒序排列
     * 包含已读和未读消息，前端可根据status字段区分消息状态
     * 需要JWT令牌认证
     *
     * @param request 消息查询请求参数
     *                - pageNum: 页码，从1开始，默认为1
     *                - pageSize: 每页大小，默认为10，最大为100
     * @return 分页消息列表，包含消息详情和分页信息
     */
    @GetMapping
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "NOTIFICATION_LIST", name = "消息列表")})
    public ApiResponse<IPage<NotificationDTO>> getNotifications(NotificationQueryRequest request) {
        String userId = UserContext.getCurrentUserId();
        IPage<NotificationDTO> notifications =
                notificationAppService.getUserNotifications(userId, request);
        return ApiResponse.success(notifications);
    }

    /**
     * 获取未读消息数量
     * 获取当前用户的未读消息总数，用于前端显示红点数字
     * 只统计站内消息(IN_APP)且状态为已发送(SENT)的消息
     * 需要JWT令牌认证
     *
     * @return 未读消息数量
     */
    @GetMapping("/unread-count")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "NOTIFICATION_UNREAD_COUNT", name = "未读消息数")})
    public ApiResponse<Long> getUnreadCount() {
        String userId = UserContext.getCurrentUserId();
        Long count = notificationAppService.getUnreadNotificationCount(userId);
        return ApiResponse.success(count);
    }

    /**
     * 标记单个消息为已读
     * 将指定的消息标记为已读状态，只能操作属于当前用户的消息
     * 标记成功后，该消息状态将从SENT变更为READ
     * 需要JWT令牌认证
     *
     * @param notificationId 消息ID，UUID格式
     * @return 操作成功状态
     */
    @PutMapping("/{notificationId}/read")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "NOTIFICATION_MARK_READ", name = "标记已读")})
    public ApiResponse<Void> markAsRead(
            @PathVariable String notificationId) {
        String userId = UserContext.getCurrentUserId();
        notificationAppService.markNotificationAsRead(userId, notificationId);
        return ApiResponse.success("消息已标记为已读");
    }

    /**
     * 标记所有消息为已读
     * 将当前用户的所有未读消息批量标记为已读状态
     * 只影响状态为SENT的站内消息，已读消息不受影响
     * 操作完成后，用户的未读消息数量将变为0
     * 需要JWT令牌认证
     *
     * @return 操作成功状态
     */
    @PutMapping("/read-all")
    @RequiresPlanPermissions(items = {@RequiresPlanPermissions.Item(code = "NOTIFICATION_MARK_ALL_READ", name = "全部标记已读")})
    public ApiResponse<Void> markAllAsRead() {
        String userId = UserContext.getCurrentUserId();
        notificationAppService.markAllNotificationsAsRead(userId);
        return ApiResponse.success("所有消息已标记为已读");
    }
}
