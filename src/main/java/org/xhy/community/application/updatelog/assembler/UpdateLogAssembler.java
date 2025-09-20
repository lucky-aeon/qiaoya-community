package org.xhy.community.application.updatelog.assembler;

import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.xhy.community.application.updatelog.dto.UpdateLogDTO;
import org.xhy.community.application.updatelog.dto.UpdateLogChangeDTO;
import org.xhy.community.domain.updatelog.entity.UpdateLogEntity;
import org.xhy.community.domain.updatelog.entity.UpdateLogChangeEntity;
import org.xhy.community.domain.updatelog.query.UpdateLogQuery;
import org.xhy.community.interfaces.updatelog.request.CreateUpdateLogRequest;
import org.xhy.community.interfaces.updatelog.request.UpdateUpdateLogRequest;
import org.xhy.community.interfaces.updatelog.request.CreateChangeRequest;
import org.xhy.community.interfaces.updatelog.request.AdminUpdateLogQueryRequest;

import java.util.ArrayList;
import java.util.List;

public class UpdateLogAssembler {

    public static UpdateLogDTO toDTO(UpdateLogEntity entity) {
        if (entity == null) {
            return null;
        }

        UpdateLogDTO dto = new UpdateLogDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public static UpdateLogDTO toDTOWithChanges(UpdateLogEntity entity, List<UpdateLogChangeEntity> changes) {
        if (entity == null) {
            return null;
        }

        UpdateLogDTO dto = toDTO(entity);
        dto.setChanges(toChangeDTOList(changes));
        return dto;
    }

    public static List<UpdateLogDTO> toDTOList(List<UpdateLogEntity> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }

        List<UpdateLogDTO> dtoList = new ArrayList<>();
        for (UpdateLogEntity entity : entities) {
            dtoList.add(toDTO(entity));
        }
        return dtoList;
    }

    public static UpdateLogChangeDTO toChangeDTO(UpdateLogChangeEntity entity) {
        if (entity == null) {
            return null;
        }

        UpdateLogChangeDTO dto = new UpdateLogChangeDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public static List<UpdateLogChangeDTO> toChangeDTOList(List<UpdateLogChangeEntity> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }

        List<UpdateLogChangeDTO> dtoList = new ArrayList<>();
        for (UpdateLogChangeEntity entity : entities) {
            dtoList.add(toChangeDTO(entity));
        }
        return dtoList;
    }

    public static UpdateLogEntity fromDTO(UpdateLogDTO dto) {
        if (dto == null) {
            return null;
        }

        UpdateLogEntity entity = new UpdateLogEntity();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    public static UpdateLogChangeEntity fromChangeDTO(UpdateLogChangeDTO dto) {
        if (dto == null) {
            return null;
        }

        UpdateLogChangeEntity entity = new UpdateLogChangeEntity();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    // ========== 聚合根转换方法 ==========

    /**
     * 从创建请求转换为UpdateLogEntity（聚合根）
     */
    public static UpdateLogEntity fromCreateRequest(CreateUpdateLogRequest request, String authorId) {
        if (request == null) {
            return null;
        }

        UpdateLogEntity entity = new UpdateLogEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setAuthorId(authorId);
        return entity;
    }

    /**
     * 从更新请求转换为UpdateLogEntity（聚合根）
     */
    public static UpdateLogEntity fromUpdateRequest(UpdateUpdateLogRequest request, String updateLogId) {
        if (request == null) {
            return null;
        }

        UpdateLogEntity entity = new UpdateLogEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(updateLogId);
        return entity;
    }

    /**
     * 从变更请求列表转换为UpdateLogChangeEntity列表（聚合内实体）
     */
    public static List<UpdateLogChangeEntity> fromChangeRequests(List<CreateChangeRequest> requests, String updateLogId) {
        if (CollectionUtils.isEmpty(requests)) {
            return new ArrayList<>();
        }

        List<UpdateLogChangeEntity> entities = new ArrayList<>();
        for (CreateChangeRequest request : requests) {
            UpdateLogChangeEntity entity = new UpdateLogChangeEntity();
            BeanUtils.copyProperties(request, entity);
            entity.setUpdateLogId(updateLogId);
            entities.add(entity);
        }
        return entities;
    }

    /**
     * 从管理员查询请求转换为领域查询对象
     */
    public static UpdateLogQuery fromAdminQueryRequest(AdminUpdateLogQueryRequest request) {
        if (request == null) {
            return null;
        }
        UpdateLogQuery query = new UpdateLogQuery(request.getPageNum(), request.getPageSize());
        query.setStatus(request.getStatus());
        query.setVersion(request.getVersion());
        query.setTitle(request.getTitle());
        return query;
    }
}
