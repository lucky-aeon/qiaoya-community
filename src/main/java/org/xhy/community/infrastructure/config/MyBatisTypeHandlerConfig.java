package org.xhy.community.infrastructure.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhy.community.domain.comment.valueobject.BusinessType;
import org.xhy.community.domain.course.valueobject.CourseStatus;
import org.xhy.community.domain.post.valueobject.CategoryType;
import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.domain.resource.valueobject.ResourceType;
import org.xhy.community.domain.subscription.valueobject.SubscriptionPlanStatus;
import org.xhy.community.domain.subscription.valueobject.SubscriptionStatus;
import org.xhy.community.domain.user.valueobject.UserStatus;
import org.xhy.community.domain.user.valueobject.UserRole;
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.cdk.valueobject.CDKStatus;
import org.xhy.community.domain.cdk.valueobject.CDKAcquisitionType;
import org.xhy.community.domain.order.valueobject.OrderType;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.domain.follow.valueobject.FollowStatus;
import org.xhy.community.domain.notification.valueobject.NotificationType;
import org.xhy.community.domain.notification.valueobject.ChannelType;
import org.xhy.community.domain.notification.valueobject.NotificationStatus;
import org.xhy.community.domain.updatelog.valueobject.UpdateLogStatus;
import org.xhy.community.domain.updatelog.valueobject.ChangeType;
import org.xhy.community.domain.testimonial.valueobject.TestimonialStatus;
import org.xhy.community.infrastructure.converter.BusinessTypeConverter;
import org.xhy.community.infrastructure.converter.CategoryTypeConverter;
import org.xhy.community.infrastructure.converter.CourseStatusConverter;
import org.xhy.community.infrastructure.converter.PostStatusConverter;
import org.xhy.community.infrastructure.converter.ResourceTypeConverter;
import org.xhy.community.infrastructure.converter.SubscriptionPlanStatusConverter;
import org.xhy.community.infrastructure.converter.SubscriptionStatusConverter;
import org.xhy.community.infrastructure.converter.UserStatusConverter;
import org.xhy.community.infrastructure.converter.UserRoleConverter;
import org.xhy.community.infrastructure.converter.CDKTypeConverter;
import org.xhy.community.infrastructure.converter.CDKStatusConverter;
import org.xhy.community.infrastructure.converter.CDKAcquisitionTypeConverter;
import org.xhy.community.infrastructure.converter.OrderTypeConverter;
import org.xhy.community.infrastructure.converter.ActivityTypeConverter;
import org.xhy.community.infrastructure.converter.FollowTargetTypeConverter;
import org.xhy.community.infrastructure.converter.FollowStatusConverter;
import org.xhy.community.infrastructure.converter.NotificationTypeConverter;
import org.xhy.community.infrastructure.converter.ChannelTypeConverter;
import org.xhy.community.infrastructure.converter.NotificationStatusConverter;
import org.xhy.community.infrastructure.converter.UpdateLogStatusConverter;
import org.xhy.community.infrastructure.converter.ChangeTypeConverter;
import org.xhy.community.infrastructure.converter.TestimonialStatusConverter;
import org.xhy.community.domain.course.valueobject.CourseResource;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.infrastructure.converter.SystemConfigTypeConverter;
import org.xhy.community.infrastructure.converter.*;
import org.xhy.community.domain.post.valueobject.QAResolveStatus;
import org.xhy.community.infrastructure.converter.QAResolveStatusConverter;

import jakarta.annotation.PostConstruct;

import java.util.List;

/** MyBatis类型处理器配置类 用于手动注册类型处理器 */
@Configuration
public class MyBatisTypeHandlerConfig {

    private static final Logger log = LoggerFactory.getLogger(MyBatisTypeHandlerConfig.class);

    private final SqlSessionFactory sqlSessionFactory;

    public MyBatisTypeHandlerConfig(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /** 初始化注册类型处理器 */
    @PostConstruct
    public void registerTypeHandlers() {
        TypeHandlerRegistry typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();

        // 注册枚举类型处理器
        typeHandlerRegistry.register(UserStatus.class, new UserStatusConverter());
        typeHandlerRegistry.register(UserRole.class, new UserRoleConverter());
        typeHandlerRegistry.register(PostStatus.class, new PostStatusConverter());
        typeHandlerRegistry.register(CategoryType.class, new CategoryTypeConverter());
        typeHandlerRegistry.register(CourseStatus.class, new CourseStatusConverter());
        typeHandlerRegistry.register(BusinessType.class, new BusinessTypeConverter());
        typeHandlerRegistry.register(QAResolveStatus.class, new QAResolveStatusConverter());
        typeHandlerRegistry.register(ResourceType.class, new ResourceTypeConverter());
        typeHandlerRegistry.register(SubscriptionPlanStatus.class, new SubscriptionPlanStatusConverter());
        typeHandlerRegistry.register(SubscriptionStatus.class, new SubscriptionStatusConverter());
        typeHandlerRegistry.register(CDKType.class, new CDKTypeConverter());
        typeHandlerRegistry.register(CDKStatus.class, new CDKStatusConverter());
        typeHandlerRegistry.register(CDKAcquisitionType.class, new CDKAcquisitionTypeConverter());
        typeHandlerRegistry.register(OrderType.class, new OrderTypeConverter());
        typeHandlerRegistry.register(ActivityType.class, new ActivityTypeConverter());
        typeHandlerRegistry.register(FollowTargetType.class, new FollowTargetTypeConverter());
        typeHandlerRegistry.register(FollowStatus.class, new FollowStatusConverter());

        typeHandlerRegistry.register(NotificationType.class, new NotificationTypeConverter());
        typeHandlerRegistry.register(ChannelType.class, new ChannelTypeConverter());
        typeHandlerRegistry.register(NotificationStatus.class, new NotificationStatusConverter());
        typeHandlerRegistry.register(SystemConfigType.class, new SystemConfigTypeConverter());
        typeHandlerRegistry.register(UpdateLogStatus.class, new UpdateLogStatusConverter());
        typeHandlerRegistry.register(ChangeType.class, new ChangeTypeConverter());
        typeHandlerRegistry.register(TestimonialStatus.class, new TestimonialStatusConverter());

        // 注册集合类型处理器
        typeHandlerRegistry.register(java.util.Map.class, new MapJsonTypeHandler());

        // 注意：List类型处理器不进行全局注册，避免类型冲突
        // 依赖@TableField(typeHandler = XxxConverter.class)注解来指定具体的转换器
        // CourseEntity中的字段会根据@TableField注解自动选择对应的转换器

        log.info("手动注册类型处理器：UserStatusConverter, UserRoleConverter, PostStatusConverter, CategoryTypeConverter, CourseStatusConverter, BusinessTypeConverter, QAResolveStatusConverter, ResourceTypeConverter, SubscriptionPlanStatusConverter, SubscriptionStatusConverter, CDKTypeConverter, CDKStatusConverter, CDKAcquisitionTypeConverter, OrderTypeConverter, ActivityTypeConverter, FollowTargetTypeConverter, FollowStatusConverter, NotificationTypeConverter, ChannelTypeConverter, NotificationStatusConverter, UpdateLogStatusConverter, ChangeTypeConverter, TestimonialStatusConverter, MapJsonTypeHandler");
        log.info("已注册的类型处理器总数: {}", typeHandlerRegistry.getTypeHandlers().size());
    }
}
