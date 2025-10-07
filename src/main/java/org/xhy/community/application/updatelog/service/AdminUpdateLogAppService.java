package org.xhy.community.application.updatelog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.updatelog.assembler.UpdateLogAssembler;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.updatelog.dto.UpdateLogDTO;
import org.xhy.community.domain.notification.context.UpdateLogPublishedNotificationData;
import org.xhy.community.domain.notification.valueobject.BatchSendConfig;
import org.xhy.community.domain.updatelog.entity.UpdateLogEntity;
import org.xhy.community.domain.updatelog.entity.UpdateLogChangeEntity;
import org.xhy.community.domain.updatelog.service.UpdateLogDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.interfaces.updatelog.request.CreateUpdateLogRequest;
import org.xhy.community.interfaces.updatelog.request.UpdateUpdateLogRequest;
import org.xhy.community.interfaces.updatelog.request.AdminUpdateLogQueryRequest;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminUpdateLogAppService {

    private final UpdateLogDomainService updateLogDomainService;
    private final UserDomainService userDomainService;
    private final org.xhy.community.domain.notification.service.NotificationDomainService notificationDomainService;

    public AdminUpdateLogAppService(UpdateLogDomainService updateLogDomainService,
                                   UserDomainService userDomainService,
                                   org.xhy.community.domain.notification.service.NotificationDomainService notificationDomainService) {
        this.updateLogDomainService = updateLogDomainService;
        this.userDomainService = userDomainService;
        this.notificationDomainService = notificationDomainService;
    }

    /**
     * 创建更新日志聚合
     * 接收完整的Request对象，包含日志信息和变更详情
     */
    @Transactional(rollbackFor = Exception.class)
    public UpdateLogDTO createUpdateLog(CreateUpdateLogRequest request, String authorId) {
        // 转换Request为聚合实体
        UpdateLogEntity updateLog = UpdateLogAssembler.fromCreateRequest(request, authorId);
        List<UpdateLogChangeEntity> changes = UpdateLogAssembler.fromChangeRequests(request.getChanges(), null);

        // 使用聚合操作创建
        UpdateLogEntity createdUpdateLog = updateLogDomainService.createUpdateLogAggregate(updateLog, changes);

        // 获取完整聚合并返回
        return getUpdateLogById(createdUpdateLog.getId());
    }

    /**
     * 更新更新日志聚合
     * 全量更新，包含日志信息和变更详情
     */
    @Transactional(rollbackFor = Exception.class)
    public UpdateLogDTO updateUpdateLog(String updateLogId, UpdateUpdateLogRequest request) {
        // 转换Request为聚合实体
        UpdateLogEntity updateLog = UpdateLogAssembler.fromUpdateRequest(request, updateLogId);
        List<UpdateLogChangeEntity> changes = UpdateLogAssembler.fromChangeRequests(request.getChanges(), updateLogId);

        // 使用聚合操作更新
        UpdateLogEntity updatedUpdateLog = updateLogDomainService.updateUpdateLogAggregate(updateLog, changes);

        // 获取完整聚合并返回
        return getUpdateLogById(updatedUpdateLog.getId());
    }

    /**
     * 获取更新日志详情
     * 包含完整的变更详情列表和作者名称
     */
    public UpdateLogDTO getUpdateLogById(String updateLogId) {
        UpdateLogEntity updateLog = updateLogDomainService.getUpdateLogAggregateById(updateLogId);
        List<UpdateLogChangeEntity> changes = updateLogDomainService.getChangesByUpdateLogId(updateLogId);

        UpdateLogDTO dto = UpdateLogAssembler.toDTOWithChanges(updateLog, changes);

        // 动态获取作者名称
        if (updateLog.getAuthorId() != null) {
            String authorName = userDomainService.getUserById(updateLog.getAuthorId()).getName();
            dto.setAuthorName(authorName);
        }

        return dto;
    }

    /**
     * 删除更新日志聚合
     * 级联删除日志及所有变更详情
     */
    public void deleteUpdateLog(String updateLogId) {
        updateLogDomainService.deleteUpdateLogAggregate(updateLogId);
    }

    /**
     * 分页查询更新日志
     * 支持状态、版本号、标题筛选，批量获取作者名称
     */
    public IPage<UpdateLogDTO> getUpdateLogs(AdminUpdateLogQueryRequest request) {
        // 使用查询对象封装参数
        var query = UpdateLogAssembler.fromAdminQueryRequest(request);
        IPage<UpdateLogEntity> entityPage = updateLogDomainService.queryUpdateLogs(query);

        List<UpdateLogEntity> updateLogs = entityPage.getRecords();
        if (updateLogs.isEmpty()) {
            return entityPage.convert(UpdateLogAssembler::toDTO);
        }

        // 批量获取作者信息
        Set<String> authorIds = updateLogs.stream()
                .map(UpdateLogEntity::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<String, UserEntity> authorNameMap = userDomainService.getUserEntityMapByIds(authorIds);

        // 批量获取每个更新日志的变更详情（避免N+1）
        Set<String> updateLogIds = updateLogs.stream()
                .map(UpdateLogEntity::getId)
                .collect(Collectors.toSet());
        Map<String, List<UpdateLogChangeEntity>> changesMap = updateLogDomainService.getChangesByUpdateLogIds(updateLogIds);

        // 组装包含变更详情的DTO
        List<UpdateLogDTO> dtoList = updateLogs.stream()
                .map(entity -> {
                    List<UpdateLogChangeEntity> changes = changesMap.getOrDefault(entity.getId(), List.of());
                    UpdateLogDTO dto = UpdateLogAssembler.toDTOWithChanges(entity, changes);
                    if (entity.getAuthorId() != null) {
                        dto.setAuthorName(authorNameMap.get(entity.getAuthorId()).getName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        // 构建分页结果
        IPage<UpdateLogDTO> dtoPage = entityPage.convert(entity -> null);
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    /**
     * 切换更新日志状态
     * 在草稿和发布状态之间切换
     */
    public UpdateLogDTO toggleUpdateLogStatus(String updateLogId) {
        UpdateLogEntity updatedUpdateLog = updateLogDomainService.toggleUpdateLogStatus(updateLogId);

        // 若切换后为 PUBLISHED，则进行全员通知（站内必发，邮件尊重用户偏好）
        if (updatedUpdateLog != null && updatedUpdateLog.isPublished()) {
            broadcastUpdateLogPublished(updatedUpdateLog);
        }

        return UpdateLogAssembler.toDTO(updatedUpdateLog);
    }

    /**
     * 全员广播：更新日志发布
     * Application层负责编排：分页获取活跃用户 -> 组装通知数据 -> 批量发送
     */
    private void broadcastUpdateLogPublished(UpdateLogEntity updateLog) {
        // 分页遍历活跃用户
        final int pageSize = 500;
        int pageNum = 1;

        while (true) {
            var query = new org.xhy.community.domain.user.query.UserQuery(pageNum, pageSize);
            query.setStatus(org.xhy.community.domain.user.valueobject.UserStatus.ACTIVE);

            com.baomidou.mybatisplus.core.metadata.IPage<org.xhy.community.domain.user.entity.UserEntity> page =
                    userDomainService.queryUsers(query);

            java.util.List<org.xhy.community.domain.user.entity.UserEntity> users = page.getRecords();
            if (users == null || users.isEmpty()) break;

            // 构建通知数据
            List<UpdateLogPublishedNotificationData> notifications =
                    new ArrayList<>(users.size());
            for (var user : users) {
                notifications.add(new UpdateLogPublishedNotificationData(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getEmailNotificationEnabled() != null ? user.getEmailNotificationEnabled() : Boolean.FALSE,
                        updateLog.getVersion(),
                        updateLog.getTitle(),
                        "/dashboard/changelog"
                ));
            }

            // 批量发送（站内 + 邮件），邮件尊重用户开关
            var config = new BatchSendConfig()
                    .withBatchSize(200)
                    .withDelayBetweenBatches(0)
                    .withSkipOnError(true)
                    .withLogDetail(false);

            notificationDomainService.sendBatchNotifications(notifications, config);

            if (page.getCurrent() >= page.getPages()) {
                break;
            }
            pageNum++;
        }
    }
}
