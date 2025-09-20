package org.xhy.community.application.updatelog.service;

import org.springframework.stereotype.Service;
import org.xhy.community.application.updatelog.assembler.UpdateLogAssembler;
import org.xhy.community.application.updatelog.dto.UpdateLogDTO;
import org.xhy.community.domain.updatelog.entity.UpdateLogEntity;
import org.xhy.community.domain.updatelog.entity.UpdateLogChangeEntity;
import org.xhy.community.domain.updatelog.service.UpdateLogDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 前台更新日志应用服务
 * 提供面向前台用户的更新日志查询功能
 */
@Service
public class UpdateLogAppService {

    private final UpdateLogDomainService updateLogDomainService;
    private final UserDomainService userDomainService;

    public UpdateLogAppService(UpdateLogDomainService updateLogDomainService,
                              UserDomainService userDomainService) {
        this.updateLogDomainService = updateLogDomainService;
        this.userDomainService = userDomainService;
    }

    /**
     * 获取已发布的更新日志列表
     * 返回已发布状态的完整更新日志聚合根，包含所有变更详情和作者名称
     * 使用批量查询避免 N+1 问题
     *
     * @return 已发布的更新日志完整列表
     */
    public List<UpdateLogDTO> getPublishedUpdateLogs() {
        List<UpdateLogEntity> updateLogs = updateLogDomainService.getPublishedUpdateLogs();

        if (updateLogs.isEmpty()) {
            return List.of();
        }

        // 批量获取作者名称
        Set<String> authorIds = updateLogs.stream()
                .map(UpdateLogEntity::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, UserEntity> authorNameMap = userDomainService.getUserEntityMapByIds(authorIds);

        // 批量获取所有变更详情（避免 N+1 查询）
        Set<String> updateLogIds = updateLogs.stream()
                .map(UpdateLogEntity::getId)
                .collect(Collectors.toSet());

        Map<String, List<UpdateLogChangeEntity>> changesMap = updateLogDomainService.getChangesByUpdateLogIds(updateLogIds);

        // 转换为完整的DTO
        return updateLogs.stream()
                .map(entity -> {
                    // 从批量查询结果中获取变更详情
                    List<UpdateLogChangeEntity> changes = changesMap.getOrDefault(entity.getId(), List.of());

                    // 转换为完整聚合DTO
                    UpdateLogDTO dto = UpdateLogAssembler.toDTOWithChanges(entity, changes);

                    // 设置作者名称
                    if (entity.getAuthorId() != null) {
                        dto.setAuthorName(authorNameMap.get(entity.getAuthorId()).getName());
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
}