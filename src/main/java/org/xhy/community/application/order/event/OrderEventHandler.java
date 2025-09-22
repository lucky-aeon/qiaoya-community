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

@Component
public class OrderEventHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderEventHandler.class);

    private final OrderDomainService orderDomainService;
    private final CourseDomainService courseDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;

    public OrderEventHandler(OrderDomainService orderDomainService,
                           CourseDomainService courseDomainService,
                           SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.orderDomainService = orderDomainService;
        this.courseDomainService = courseDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
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

            // 获取商品名称
            String productName = getProductName(event.getCdkType(), event.getTargetId());

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
                event.getPrice(),
                event.getActivatedTime()
            );

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
    private String getProductName(org.xhy.community.domain.cdk.valueobject.CDKType cdkType, String targetId) {
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

    private String maskCdkCode(String cdkCode) {
        if (cdkCode == null || cdkCode.length() <= 4) return "****";
        int len = cdkCode.length();
        return cdkCode.substring(0, Math.min(4, len)) + "****" + cdkCode.substring(len - 2);
    }
}