package org.xhy.community.infrastructure.ai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ChatAIClientTest {


    @Autowired
    ChatAIClient chatAIClient;

    @Autowired
    Environment env;

    @Test
    void chat_should_return_non_empty_when_api_key_present() {
        String apiKey1 = env.getProperty("spring.ai.openai.api-key");
        String apiKey2 = System.getenv("SPRING_AI_OPENAI_API_KEY");
        String apiKey3 = System.getenv("OPENAI_API_KEY");
        Assumptions.assumeTrue(
                (apiKey1 != null && !apiKey1.isBlank()) ||
                (apiKey2 != null && !apiKey2.isBlank()) ||
                (apiKey3 != null && !apiKey3.isBlank()),
                "Skipping test: OpenAI API key not provided"
        );

        String userPrompt = "你是谁";

        String result = chatAIClient.chat("", userPrompt);

        assertNotNull(result);
        assertFalse(result.isBlank());
        System.out.println("AI 回复：" + result);
    }

    @Test
    void chat_without_system_prompt_should_work() {
        String apiKey1 = env.getProperty("spring.ai.openai.api-key");
        String apiKey2 = System.getenv("SPRING_AI_OPENAI_API_KEY");
        String apiKey3 = System.getenv("OPENAI_API_KEY");
        Assumptions.assumeTrue(
                (apiKey1 != null && !apiKey1.isBlank()) ||
                (apiKey2 != null && !apiKey2.isBlank()) ||
                (apiKey3 != null && !apiKey3.isBlank()),
                "Skipping test: OpenAI API key not provided"
        );

        String userPrompt = "用一句话解释什么是 DDD（领域驱动设计）";

        String result = chatAIClient.chat(userPrompt);

        assertNotNull(result);
        assertFalse(result.isBlank());
        System.out.println("AI 回复：" + result);
    }
}
