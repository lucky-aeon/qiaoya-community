package org.xhy.community.application.notification.assembler;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.xhy.community.application.notification.dto.NotificationDTO;
import org.xhy.community.domain.notification.entity.NotificationEntity;
import org.xhy.community.domain.notification.query.NotificationQuery;
import org.xhy.community.interfaces.notification.request.NotificationQueryRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 通知转换器
 */
public class NotificationAssembler {
    
    /**
     * 实体转DTO
     */
    public static NotificationDTO toDTO(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }
        
        NotificationDTO dto = new NotificationDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 枚举转字符串
        if (entity.getType() != null) {
            dto.setType(entity.getType().getCode());
        }
        if (entity.getChannelType() != null) {
            dto.setChannelType(entity.getChannelType().getCode());
        }
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().getCode());
        }
        
        return dto;
    }
    
    /**
     * 实体列表转DTO列表
     */
    public static List<NotificationDTO> toDTOList(List<NotificationEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(NotificationAssembler::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 分页实体转分页DTO
     */
    public static IPage<NotificationDTO> toDTOPage(IPage<NotificationEntity> entityPage) {
        if (entityPage == null) {
            return null;
        }
        
        Page<NotificationDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize());
        dtoPage.setTotal(entityPage.getTotal());
        
        List<NotificationDTO> dtoList = toDTOList(entityPage.getRecords());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    /**
     * 请求对象转查询对象
     */
    public static NotificationQuery toQuery(NotificationQueryRequest request, String userId) {
        if (request == null) {
            return null;
        }

        NotificationQuery query = new NotificationQuery();
        BeanUtils.copyProperties(request, query);
        query.setUserId(userId);
        return query;
    }
}