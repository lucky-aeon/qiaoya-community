package org.xhy.community.application.order.assembler;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.xhy.community.application.order.dto.OrderDTO;
import org.xhy.community.domain.order.entity.OrderEntity;
import org.xhy.community.domain.order.query.OrderQuery;
import org.xhy.community.interfaces.order.request.OrderQueryRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderAssembler {

    /**
     * 实体转DTO
     */
    public static OrderDTO toDTO(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    /**
     * 实体列表转DTO列表
     */
    public static List<OrderDTO> toDTOList(List<OrderEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
            .map(OrderAssembler::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * 分页实体转分页DTO
     */
    public static IPage<OrderDTO> toDTOPage(IPage<OrderEntity> entityPage) {
        IPage<OrderDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<OrderDTO> dtoList = toDTOList(entityPage.getRecords());
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    /**
     * 带用户名称的实体列表转DTO列表
     */
    public static List<OrderDTO> toDTOList(List<OrderEntity> entities, Map<String, String> userNameMap) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
            .map(entity -> {
                OrderDTO dto = toDTO(entity);
                if (dto != null && userNameMap != null) {
                    dto.setUserName(userNameMap.get(entity.getUserId()));
                }
                return dto;
            })
            .collect(Collectors.toList());
    }

    /**
     * 请求对象转查询对象
     */
    public static OrderQuery toQuery(OrderQueryRequest request) {
        if (request == null) {
            return null;
        }

        OrderQuery query = new OrderQuery();
        BeanUtils.copyProperties(request, query);
        return query;
    }
}