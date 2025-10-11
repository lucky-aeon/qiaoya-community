package org.xhy.community.infrastructure.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 通用 AI 聊天客户端
 * <p>
 * 位于 Infrastructure 层，提供纯技术实现，不包含业务逻辑。
 * 可被多种业务场景复用（评论总结、文章生成、智能问答等）。
 */
@Component
public class ChatAIClient {

    private final ChatClient chatClient;

    public ChatAIClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 执行 AI 对话请求
     *
     * @param systemPrompt 系统提示词（定义 AI 角色和规则）
     * @param userPrompt   用户输入内容
     * @return AI 生成的文本响应
     */
    public String chat(String systemPrompt, String userPrompt) {
        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }

    /**
     * 执行简单的 AI 对话请求（仅用户提示词）
     *
     * @param userPrompt 用户输入内容
     * @return AI 生成的文本响应
     */
    public String chat(String userPrompt) {
        return chatClient
                .prompt()
                .user(userPrompt)
                .call()
                .content();
    }
}
