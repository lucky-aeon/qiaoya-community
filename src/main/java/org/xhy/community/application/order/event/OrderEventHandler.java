package org.xhy.community.application.order.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.xhy.community.domain.cdk.event.CDKActivatedEvent;
import org.xhy.community.domain.cdk.valueobject.CDKAcquisitionType;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.order.entity.OrderEntity;
import org.xhy.community.domain.order.service.OrderDomainService;
import org.xhy.community.domain.order.valueobject.OrderType;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.domain.cdk.service.CDKDomainService;
import org.xhy.community.domain.cdk.entity.CDKEntity;
import org.xhy.community.domain.cdk.valueobject.CDKType;

@Component
public class OrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);

    private final OrderDomainService orderDomainService;
    private final CourseDomainService courseDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;
    private final CDKDomainService cdkDomainService;

    public OrderEventHandler(OrderDomainService orderDomainService,
                           CourseDomainService courseDomainService,
                           SubscriptionPlanDomainService subscriptionPlanDomainService,
                           CDKDomainService cdkDomainService) {
        this.orderDomainService = orderDomainService;
        this.courseDomainService = courseDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
        this.cdkDomainService = cdkDomainService;
    }

    /**
     * 处理CDK激活事件，自动创建订单记录
     */
    @EventListener
    public void handleCDKActivated(CDKActivatedEvent event) {
        try {
            log.info("[订单创建] 开始处理CDK激活事件: userId={}, cdkCode={}, type={}",
                    event.getUserId(), maskCdkCode(event.getCdkCode()), event.getCdkType());

            // 检查是否已存在订单记录
            OrderEntity existingOrder = orderDomainService.getOrderByCdkCode(event.getCdkCode());
            if (existingOrder != null) {
                log.warn("[订单创建] CDK对应订单已存在，跳过创建: cdkCode={}, orderId={}",
                        maskCdkCode(event.getCdkCode()), existingOrder.getId());
                return;
            }

            // 获取商品名称与原始价格
            String productName = getProductName(event.getCdkType(), event.getTargetId());
            java.math.BigDecimal originalPrice = getProductPrice(event.getCdkType(), event.getTargetId());

            // 获取CDK详情以决定最终金额与extra
            CDKEntity cdk = cdkDomainService.getCDKByCode(event.getCdkCode());

            // 金额优先级：GIFT → 0；否则CDK.price（非空）→ price；否则商品原价
            java.math.BigDecimal amount = java.math.BigDecimal.ZERO;
            if (event.getAcquisitionType() == CDKAcquisitionType.GIFT) {
                amount = java.math.BigDecimal.ZERO;
            } else if (cdk.getPrice() != null) {
                amount = cdk.getPrice();
            } else {
                amount = originalPrice != null ? originalPrice : java.math.BigDecimal.ZERO;
            }

            // 转换订单类型
            OrderType orderType = event.getAcquisitionType() == CDKAcquisitionType.PURCHASE ?
                                 OrderType.PURCHASE : OrderType.GIFT;

            // 创建订单记录
            OrderEntity order = new OrderEntity(
                null, // orderNo由service生成
                event.getUserId(),
                event.getCdkCode(),
                event.getCdkType(),
                event.getTargetId(),
                productName,
                orderType,
                amount,
                event.getActivatedTime()
            );

            // 组装 extra 信息
            java.util.Map<String, Object> extra = new java.util.HashMap<>();
            extra.put("acquisitionType", event.getAcquisitionType() != null ? event.getAcquisitionType().name() : null);
            extra.put("subscriptionStrategy", cdk.getSubscriptionStrategy() != null ? cdk.getSubscriptionStrategy().name() : null);
            extra.put("cdkPrice", cdk.getPrice());
            extra.put("productOriginalPrice", originalPrice);
            extra.put("cdkRemark", cdk.getRemark());
            order.setExtra(extra);

            OrderEntity createdOrder = orderDomainService.createOrder(order);

            log.info("[订单创建] 成功创建订单: orderId={}, orderNo={}, type={}, amount={}",
                    createdOrder.getId(), createdOrder.getOrderNo(),
                    createdOrder.getOrderType(), createdOrder.getAmount());

        } catch (Exception e) {
            log.error("[订单创建] 处理CDK激活事件失败: userId={}, cdkCode={}",
                     event.getUserId(), maskCdkCode(event.getCdkCode()), e);
            // 不抛出异常，避免影响CDK激活流程
        }
    }

    /**
     * 根据商品类型和ID获取商品名称
     */
    private String getProductName(CDKType cdkType, String targetId) {
        return switch (cdkType) {
            case COURSE -> {
                try {
                    yield courseDomainService.getCourseById(targetId).getTitle();
                } catch (Exception e) {
                    log.warn("[订单创建] 获取课程名称失败: courseId={}", targetId, e);
                    yield "未知课程";
                }
            }
            case SUBSCRIPTION_PLAN -> {
                try {
                    yield subscriptionPlanDomainService.getSubscriptionPlanById(targetId).getName();
                } catch (Exception e) {
                    log.warn("[订单创建] 获取订阅计划名称失败: planId={}", targetId, e);
                    yield "未知订阅计划";
                }
            }
        };
    }

    private java.math.BigDecimal getProductPrice(CDKType cdkType, String targetId) {
        return switch (cdkType) {
            case COURSE -> {
                try {
                    yield courseDomainService.getCourseById(targetId).getPrice();
                } catch (Exception e) {
                    log.warn("[订单创建] 获取课程价格失败: courseId={}", targetId, e);
                    yield java.math.BigDecimal.ZERO;
                }
            }
            case SUBSCRIPTION_PLAN -> {
                try {
                    yield subscriptionPlanDomainService.getSubscriptionPlanById(targetId).getPrice();
                } catch (Exception e) {
                    log.warn("[订单创建] 获取订阅计划价格失败: planId={}", targetId, e);
                    yield java.math.BigDecimal.ZERO;
                }
            }
        };
    }

    private String maskCdkCode(String cdkCode) {
        if (cdkCode == null || cdkCode.length() <= 4) return "****";
        int len = cdkCode.length();
        return cdkCode.substring(0, Math.min(4, len)) + "****" + cdkCode.substring(len - 2);
    }
}
