package org.xhy.community.application.ainews.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.ainews.dto.AdminDailyItemDTO;
import org.xhy.community.application.ainews.dto.DailyItemDTO;
import org.xhy.community.domain.ainews.entity.DailyItemEntity;
import org.xhy.community.domain.ainews.query.DailyItemQuery;
import org.xhy.community.domain.common.valueobject.AccessLevel;
import org.xhy.community.interfaces.ainews.request.DailyQueryRequest;

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

    /**
     * App 列表查询请求 -> 领域查询对象
     */
    public static DailyItemQuery fromAppRequest(DailyQueryRequest request) {
        if (request == null) {
            return null;
        }
        DailyItemQuery query = new DailyItemQuery(request.getPageNum(), request.getPageSize());
        query.setDate(request.getDate());
        // 默认 false，列表按需求可以在 Controller/App 层覆盖为 true
        query.setWithContent(Boolean.TRUE.equals(request.getWithContent()));
        query.setAccessLevel(AccessLevel.USER);
        return query;
    }
}
