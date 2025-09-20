package org.xhy.community.domain.updatelog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.xhy.community.domain.updatelog.entity.UpdateLogEntity;
import org.xhy.community.domain.updatelog.entity.UpdateLogChangeEntity;
import org.xhy.community.domain.updatelog.repository.UpdateLogRepository;
import org.xhy.community.domain.updatelog.repository.UpdateLogChangeRepository;
import org.xhy.community.domain.updatelog.query.UpdateLogQuery;
import org.xhy.community.domain.updatelog.valueobject.UpdateLogStatus;
import org.xhy.community.domain.updatelog.valueobject.ChangeType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.config.ValidationErrorCode;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class UpdateLogDomainService {

    private final UpdateLogRepository updateLogRepository;
    private final UpdateLogChangeRepository updateLogChangeRepository;

    public UpdateLogDomainService(UpdateLogRepository updateLogRepository,
                                 UpdateLogChangeRepository updateLogChangeRepository) {
        this.updateLogRepository = updateLogRepository;
        this.updateLogChangeRepository = updateLogChangeRepository;
    }


    public UpdateLogEntity getUpdateLogById(String updateLogId) {
        return updateLogRepository.selectById(updateLogId);
    }

    public boolean isVersionExists(String version, String excludeId) {
        LambdaQueryWrapper<UpdateLogEntity> queryWrapper = new LambdaQueryWrapper<UpdateLogEntity>()
                .eq(UpdateLogEntity::getVersion, version);

        if (StringUtils.hasText(excludeId)) {
            queryWrapper.ne(UpdateLogEntity::getId, excludeId);
        }

        return updateLogRepository.exists(queryWrapper);
    }

    public IPage<UpdateLogEntity> getUpdateLogsByStatus(UpdateLogStatus status, Integer pageNum, Integer pageSize) {
        Page<UpdateLogEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<UpdateLogEntity> queryWrapper = new LambdaQueryWrapper<UpdateLogEntity>()
                .eq(status != null, UpdateLogEntity::getStatus, status)
                .orderByDesc(UpdateLogEntity::getCreateTime);

        return updateLogRepository.selectPage(page, queryWrapper);
    }

    /**
     * 使用查询对象的方式分页查询更新日志
     */
    public IPage<UpdateLogEntity> queryUpdateLogs(UpdateLogQuery query) {
        Page<UpdateLogEntity> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<UpdateLogEntity> queryWrapper = new LambdaQueryWrapper<UpdateLogEntity>()
                .eq(query.getStatus() != null, UpdateLogEntity::getStatus, query.getStatus())
                .eq(org.springframework.util.StringUtils.hasText(query.getVersion()), UpdateLogEntity::getVersion, query.getVersion())
                .like(org.springframework.util.StringUtils.hasText(query.getTitle()), UpdateLogEntity::getTitle, query.getTitle())
                .orderByDesc(UpdateLogEntity::getCreateTime);

        return updateLogRepository.selectPage(page, queryWrapper);
    }

    public List<UpdateLogEntity> getPublishedUpdateLogs() {
        LambdaQueryWrapper<UpdateLogEntity> queryWrapper = new LambdaQueryWrapper<UpdateLogEntity>()
                .eq(UpdateLogEntity::getStatus, UpdateLogStatus.PUBLISHED)
                .orderByDesc(UpdateLogEntity::getCreateTime);

        return updateLogRepository.selectList(queryWrapper);
    }

    public List<UpdateLogChangeEntity> getChangesByUpdateLogId(String updateLogId) {
        LambdaQueryWrapper<UpdateLogChangeEntity> queryWrapper = new LambdaQueryWrapper<UpdateLogChangeEntity>()
                .eq(UpdateLogChangeEntity::getUpdateLogId, updateLogId)
                .orderByAsc(UpdateLogChangeEntity::getSortOrder);

        return updateLogChangeRepository.selectList(queryWrapper);
    }


    public void deleteChangesByUpdateLogId(String updateLogId) {
        LambdaQueryWrapper<UpdateLogChangeEntity> queryWrapper = new LambdaQueryWrapper<UpdateLogChangeEntity>()
                .eq(UpdateLogChangeEntity::getUpdateLogId, updateLogId);

        updateLogChangeRepository.delete(queryWrapper);
    }

    public void batchCreateChanges(List<UpdateLogChangeEntity> changes) {
        if (!CollectionUtils.isEmpty(changes)) {
            for (UpdateLogChangeEntity change : changes) {
                updateLogChangeRepository.insert(change);
            }
        }
    }

    // ========== 聚合根操作方法 ==========

    /**
     * 创建更新日志聚合（原子操作）
     * 同时创建日志主体和变更详情
     */
    @Transactional(rollbackFor = Exception.class)
    public UpdateLogEntity createUpdateLogAggregate(UpdateLogEntity updateLog, List<UpdateLogChangeEntity> changes) {
        // 校验版本号唯一性
        if (isVersionExists(updateLog.getVersion(), null)) {
            throw new BusinessException(ValidationErrorCode.PARAM_INVALID, "版本号已存在");
        }

        // 创建更新日志主体
        updateLogRepository.insert(updateLog);

        // 批量创建变更详情
        if (!CollectionUtils.isEmpty(changes)) {
            LocalDateTime now = LocalDateTime.now();
            for (UpdateLogChangeEntity change : changes) {
                change.setUpdateLogId(updateLog.getId());
                if (change.getId() == null) {
                    // 保证批量插入时ID/时间字段完整
                    change.setId(java.util.UUID.randomUUID().toString());
                    change.setCreateTime(now);
                    change.setUpdateTime(now);
                    if (change.getDeleted() == null) {
                        change.setDeleted(false);
                    }
                }
            }
            // 批量插入变更详情（使用 MyBatis-Plus 循环插入，避免手写 SQL）
            this.updateLogChangeRepository.insert(changes);
        }

        return updateLog;
    }

    /**
     * 更新更新日志聚合（全量替换）
     * 先删除原有变更详情，再插入新的变更详情
     */
    public UpdateLogEntity updateUpdateLogAggregate(UpdateLogEntity updateLog, List<UpdateLogChangeEntity> changes) {
        // 校验版本号唯一性（排除当前记录）
        if (isVersionExists(updateLog.getVersion(), updateLog.getId())) {
            throw new BusinessException(ValidationErrorCode.PARAM_INVALID, "版本号已存在");
        }

        // 更新日志主体
        updateLogRepository.updateById(updateLog);

        // 删除原有变更详情
        deleteChangesByUpdateLogId(updateLog.getId());

        // 批量创建新的变更详情
        if (!CollectionUtils.isEmpty(changes)) {
            for (UpdateLogChangeEntity change : changes) {
                change.setUpdateLogId(updateLog.getId());
            }
            // 批量插入变更详情（使用 MyBatis-Plus 循环插入，避免手写 SQL）
            this.updateLogChangeRepository.insert(changes);
        }

        return updateLog;
    }

    /**
     * 删除更新日志聚合（级联删除）
     * 同时删除日志主体和所有变更详情
     */
    public void deleteUpdateLogAggregate(String updateLogId) {
        UpdateLogEntity updateLog = getUpdateLogById(updateLogId);
        if (updateLog == null) {
            throw new BusinessException(ValidationErrorCode.PARAM_INVALID, "更新日志不存在");
        }

        // 校验业务规则：已发布的日志不能删除
        if (UpdateLogStatus.PUBLISHED.equals(updateLog.getStatus())) {
            throw new BusinessException(ValidationErrorCode.PARAM_INVALID, "已发布的更新日志不能删除");
        }

        // 删除所有变更详情
        deleteChangesByUpdateLogId(updateLogId);

        // 删除日志主体
        updateLogRepository.deleteById(updateLogId);
    }

    /**
     * 获取完整的更新日志聚合
     * 包含日志主体和所有变更详情
     */
    public UpdateLogEntity getUpdateLogAggregateById(String updateLogId) {
        return getUpdateLogById(updateLogId);
        // 注意：变更详情通过getChangesByUpdateLogId()单独获取，在AppService层组装
    }

    /**
     * 切换更新日志状态
     * 在DRAFT和PUBLISHED之间切换
     */
    public UpdateLogEntity toggleUpdateLogStatus(String updateLogId) {
        UpdateLogEntity updateLog = getUpdateLogById(updateLogId);
        if (updateLog == null) {
            throw new BusinessException(ValidationErrorCode.PARAM_INVALID, "更新日志不存在");
        }

        // 切换状态
        if (UpdateLogStatus.DRAFT.equals(updateLog.getStatus())) {
            updateLog.publish();
        } else {
            updateLog.draft();
        }

        // 更新状态
        updateLogRepository.updateById(updateLog);
        return updateLog;
    }

    /**
     * 批量获取多个更新日志的变更详情
     * 避免 N+1 查询问题，一次性查询所有相关的变更详情
     *
     * @param updateLogIds 更新日志ID集合
     * @return Map<更新日志ID, 变更详情列表>
     */
    public Map<String, List<UpdateLogChangeEntity>> getChangesByUpdateLogIds(Set<String> updateLogIds) {
        if (CollectionUtils.isEmpty(updateLogIds)) {
            return Map.of();
        }

        // 批量查询所有变更详情
        LambdaQueryWrapper<UpdateLogChangeEntity> queryWrapper = new LambdaQueryWrapper<UpdateLogChangeEntity>()
                .in(UpdateLogChangeEntity::getUpdateLogId, updateLogIds)
                .orderByAsc(UpdateLogChangeEntity::getSortOrder);

        List<UpdateLogChangeEntity> allChanges = updateLogChangeRepository.selectList(queryWrapper);

        // 按 updateLogId 分组
        return allChanges.stream()
                .collect(Collectors.groupingBy(UpdateLogChangeEntity::getUpdateLogId));
    }
}
