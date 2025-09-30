package org.xhy.community.application.ainews.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.ainews.dto.AdminDailyItemDTO;
import org.xhy.community.application.ainews.dto.DailyItemDTO;
import org.xhy.community.domain.ainews.entity.DailyItemEntity;

public class DailyItemAssembler {

    public static DailyItemDTO toDTO(DailyItemEntity entity, boolean withContent) {
        if (entity == null) return null;
        DailyItemDTO dto = new DailyItemDTO();
        BeanUtils.copyProperties(entity, dto);
        if (!withContent) {
            dto.setContent(null);
        }
        return dto;
    }

    public static AdminDailyItemDTO toAdminDTO(DailyItemEntity entity) {
        if (entity == null) return null;
        AdminDailyItemDTO dto = new AdminDailyItemDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}

