package org.xhy.community.infrastructure.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfig {

    /**
     * 提供 ChatClient Bean（基于 Spring Boot 自动配置的 ChatClient.Builder）。
     * 可以在这里配置默认的系统提示词等参数。
     */
    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .build();
    }
}
