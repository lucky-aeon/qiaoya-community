package org.xhy.community.infrastructure.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.xhy.community.domain.cdk.valueobject.CDKType;
import org.xhy.community.domain.cdk.valueobject.CDKStatus;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.domain.follow.valueobject.FollowTargetType;
import org.xhy.community.domain.follow.valueobject.FollowStatus;
import org.xhy.community.domain.course.valueobject.CourseResource;
import org.xhy.community.infrastructure.converter.*;

import jakarta.annotation.PostConstruct;

import java.util.List;

/** MyBatis类型处理器配置类 用于手动注册类型处理器 */
@Configuration
public class MyBatisTypeHandlerConfig {

    private static final Logger log = LoggerFactory.getLogger(MyBatisTypeHandlerConfig.class);

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    /** 初始化注册类型处理器 */
    @PostConstruct
    public void registerTypeHandlers() {
        TypeHandlerRegistry typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();

        // 注册枚举类型处理器
        typeHandlerRegistry.register(UserStatus.class, new UserStatusConverter());
        typeHandlerRegistry.register(PostStatus.class, new PostStatusConverter());
        typeHandlerRegistry.register(CategoryType.class, new CategoryTypeConverter());
        typeHandlerRegistry.register(CourseStatus.class, new CourseStatusConverter());
        typeHandlerRegistry.register(BusinessType.class, new BusinessTypeConverter());
        typeHandlerRegistry.register(ResourceType.class, new ResourceTypeConverter());
        typeHandlerRegistry.register(SubscriptionPlanStatus.class, new SubscriptionPlanStatusConverter());
        typeHandlerRegistry.register(SubscriptionStatus.class, new SubscriptionStatusConverter());
        typeHandlerRegistry.register(CDKType.class, new CDKTypeConverter());
        typeHandlerRegistry.register(CDKStatus.class, new CDKStatusConverter());
        typeHandlerRegistry.register(ActivityType.class, new ActivityTypeConverter());
        typeHandlerRegistry.register(FollowTargetType.class, new FollowTargetTypeConverter());
        typeHandlerRegistry.register(FollowStatus.class, new FollowStatusConverter());


        // 注册集合类型处理器
        typeHandlerRegistry.register(java.util.Map.class, new MapJsonTypeHandler());

        // 注意：List类型处理器不进行全局注册，避免类型冲突
        // 依赖@TableField(typeHandler = XxxConverter.class)注解来指定具体的转换器
        // CourseEntity中的字段会根据@TableField注解自动选择对应的转换器

        log.info("手动注册类型处理器：UserStatusConverter, PostStatusConverter, CategoryTypeConverter, CourseStatusConverter, BusinessTypeConverter, ResourceTypeConverter, SubscriptionPlanStatusConverter, SubscriptionStatusConverter, CDKTypeConverter, CDKStatusConverter, ActivityTypeConverter, FollowTargetTypeConverter, FollowStatusConverter, MapJsonTypeHandler");
        log.info("已注册的类型处理器总数: {}", typeHandlerRegistry.getTypeHandlers().size());
    }
}