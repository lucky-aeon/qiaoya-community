package org.xhy.community.infrastructure.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhy.community.domain.post.valueobject.CategoryType;
import org.xhy.community.domain.post.valueobject.PostStatus;
import org.xhy.community.domain.user.valueobject.UserStatus;
import org.xhy.community.infrastructure.converter.CategoryTypeConverter;
import org.xhy.community.infrastructure.converter.PostStatusConverter;
import org.xhy.community.infrastructure.converter.UserStatusConverter;

import jakarta.annotation.PostConstruct;

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

        log.info("手动注册类型处理器：UserStatusConverter, PostStatusConverter, CategoryTypeConverter");
        log.info("已注册的类型处理器总数: {}", typeHandlerRegistry.getTypeHandlers().size());
    }
}