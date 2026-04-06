package org.xhy.community.application.order.assembler;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.xhy.community.application.order.dto.OrderDTO;
import org.xhy.community.domain.order.entity.OrderEntity;
import org.xhy.community.domain.order.query.OrderQuery;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.order.valueobject.OrderType;
import org.xhy.community.interfaces.order.request.CreateServiceOrderRequest;
import org.xhy.community.interfaces.order.request.OrderQueryRequest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderAssembler {

    private static final String DEFAULT_SOURCE_CHANNEL = "ADMIN_MANUAL";
    private static final BigDecimal DEFAULT_QUANTITY = BigDecimal.ONE;
    private static final String DEFAULT_QUANTITY_UNIT = "次";

    /**
     * 实体转DTO
     */
    public static OrderDTO toDTO(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        OrderDTO dto = new OrderDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setTotalAmount(entity.getAmount());
        populateServiceOrderFields(dto, entity.getExtra());
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

    public static OrderEntity fromCreateServiceRequest(CreateServiceOrderRequest request,
                                                      String serviceCode,
                                                      String serviceTitleSnapshot,
                                                      String createdBy) {
        if (request == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = request.getAmount().setScale(2, RoundingMode.HALF_UP);

        OrderEntity entity = new OrderEntity();
        entity.setUserId(null);
        entity.setCdkCode(null);
        entity.setProductType(CDKType.SERVICE);
        entity.setProductId(serviceCode);
        entity.setProductName(serviceTitleSnapshot);
        entity.setOrderType(OrderType.SERVICE);
        entity.setAmount(amount);
        entity.setActivatedTime(now);
        entity.setRemark(request.getRemark());

        Map<String, Object> extra = new HashMap<>();
        extra.put("serviceCode", serviceCode);
        extra.put("serviceTitleSnapshot", serviceTitleSnapshot);
        extra.put("contactId", null);
        extra.put("sourceChannel", DEFAULT_SOURCE_CHANNEL);
        extra.put("unitPrice", amount);
        extra.put("quantity", DEFAULT_QUANTITY);
        extra.put("quantityUnit", DEFAULT_QUANTITY_UNIT);
        extra.put("totalAmount", amount);
        extra.put("status", "COMPLETED");
        extra.put("createdBy", createdBy);
        extra.put("confirmedAt", now.toString());
        extra.put("completedAt", now.toString());
        extra.put("canceledAt", null);
        entity.setExtra(extra);
        return entity;
    }

    private static void populateServiceOrderFields(OrderDTO dto, Map<String, Object> extra) {
        if (dto == null) {
            return;
        }

        dto.setContactId(asString(extra == null ? null : extra.get("contactId")));
        dto.setSourceChannel(asString(extra == null ? null : extra.get("sourceChannel")));
        dto.setUnitPrice(asBigDecimal(extra == null ? null : extra.get("unitPrice")));
        dto.setQuantity(asBigDecimal(extra == null ? null : extra.get("quantity")));
        dto.setQuantityUnit(asString(extra == null ? null : extra.get("quantityUnit")));

        String status = asString(extra == null ? null : extra.get("status"));
        if ((status == null || status.isBlank()) && dto.getOrderType() == OrderType.SERVICE) {
            status = "COMPLETED";
        }
        dto.setStatus(status);

        dto.setCreatedBy(asString(extra == null ? null : extra.get("createdBy")));

        LocalDateTime confirmedAt = asLocalDateTime(extra == null ? null : extra.get("confirmedAt"));
        LocalDateTime completedAt = asLocalDateTime(extra == null ? null : extra.get("completedAt"));
        if (dto.getOrderType() == OrderType.SERVICE) {
            LocalDateTime fallbackTime = dto.getActivatedTime();
            if (confirmedAt == null) {
                confirmedAt = fallbackTime;
            }
            if (completedAt == null) {
                completedAt = fallbackTime;
            }
        }
        dto.setConfirmedAt(confirmedAt);
        dto.setCompletedAt(completedAt);
        dto.setCanceledAt(asLocalDateTime(extra == null ? null : extra.get("canceledAt")));
        if (dto.getTotalAmount() == null) {
            dto.setTotalAmount(asBigDecimal(extra == null ? null : extra.get("totalAmount")));
        }
    }

    private static String asString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private static BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        String text = value.toString();
        if (text.isBlank()) {
            return null;
        }
        return new BigDecimal(text);
    }

    private static LocalDateTime asLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        String text = value.toString();
        if (text.isBlank()) {
            return null;
        }
        return LocalDateTime.parse(text);
    }
}
