package org.xhy.community.domain.summary.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.comment.entity.CommentEntity;
import org.xhy.community.domain.summary.entity.DiscussionSummaryEntity;
import org.xhy.community.domain.summary.repository.DiscussionSummaryRepository;
import org.xhy.community.domain.summary.valueobject.SummaryMaterial;
import org.xhy.community.domain.summary.valueobject.SummaryTargetType;
import org.xhy.community.infrastructure.ai.ChatAIClient;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscussionSummaryDomainService {

    private static final int MAX_COMMENT_COUNT = 50;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是一个专业的内容分析师，专门帮助用户快速理解%s评论区的核心内容。

            用户面临的问题：评论太多，难以快速找到有用信息。

            请按以下结构整理评论内容：

            **📋 核心问题与解答**
            - 提取用户提出的主要问题
            - 标注对应的回答和解决方案
            - 格式：Q: [问题] A: [答案/解决方案]

            **💡 有价值的信息**
            - 实用技巧和建议
            - 经验分享和最佳实践
            - 工具推荐和资源链接

            **🔥 热门讨论点**
            - 被多次提及的话题
            - 有争议但有建设性的讨论
            - 需要注意的问题和坑点

            **📝 补充信息**
            - 其他有用的补充说明
            - 相关的扩展讨论

            **🧠 分析师观点与建议**
            - 基于上述内容给出专业判断与取舍建议
            - 提供明确的下一步行动建议或决策思路
            - 说明适用场景与潜在风险，保持中立与审慎

            要求：
            - 重点提取问题和对应的答案
            - 突出实用性和可操作性
            - 如果没有明显的Q&A，则重点提取有价值的信息点
            - 保持结构清晰，便于快速阅读
            - 总结长度控制在300-600字
            - 不要引用具体评论序号（如“评论1”、“评论2”），直接描述内容
            - 在最后增加“分析师观点与建议”小节，基于材料给出你的看法
            - 观点需基于材料，不引入材料外事实；如有推断请明确标注“推断”
            - 用中文回复
            """;

    private final DiscussionSummaryRepository discussionSummaryRepository;
    private final ChatAIClient chatAIClient;

    public DiscussionSummaryDomainService(DiscussionSummaryRepository discussionSummaryRepository,
                                          ChatAIClient chatAIClient) {
        this.discussionSummaryRepository = discussionSummaryRepository;
        this.chatAIClient = chatAIClient;
    }

    public DiscussionSummaryEntity getByTarget(SummaryTargetType targetType, String targetId) {
        return discussionSummaryRepository.selectOne(
                new LambdaQueryWrapper<DiscussionSummaryEntity>()
                        .eq(DiscussionSummaryEntity::getTargetType, targetType)
                        .eq(DiscussionSummaryEntity::getTargetId, targetId)
        );
    }

    public DiscussionSummaryEntity upsert(DiscussionSummaryEntity entity) {
        DiscussionSummaryEntity exist = getByTarget(entity.getTargetType(), entity.getTargetId());
        if (exist == null) {
            discussionSummaryRepository.insert(entity);
            return entity;
        }
        entity.setId(exist.getId());
        discussionSummaryRepository.updateById(entity);
        return entity;
    }

    /**
     * 基于素材生成摘要
     */
    public String generateSummary(SummaryMaterial material) {
        String userPrompt = buildUserPrompt(material);
        String targetLabel = material.getTargetType() == SummaryTargetType.CHAPTER ? "章节" : "文章";
        return chatAIClient.chat(String.format(SYSTEM_PROMPT_TEMPLATE, targetLabel), userPrompt);
    }

    private String buildUserPrompt(SummaryMaterial material) {
        String title = nullToEmpty(material.getTitle());
        String content = nullToEmpty(material.getContent());

        String commentsJoined = material.getComments() == null ? "" : material.getComments().stream()
                .filter(c -> c.getContent() != null && !c.getContent().isBlank())
                .sorted(Comparator.comparing(CommentEntity::getCreateTime))
                .limit(MAX_COMMENT_COUNT)
                .map(c -> "- " + sanitize(c.getContent()))
                .collect(Collectors.joining("\n"));

        String targetLabel = material.getTargetType() == SummaryTargetType.CHAPTER ? "章节" : "文章";

        StringBuilder sb = new StringBuilder(2048);
        sb.append("请仅依据以下材料生成摘要：\n\n");
        sb.append("=== ").append(targetLabel).append(" ===\n")
          .append("标题：").append(title).append('\n')
          .append("正文：").append(content).append("\n\n");

        sb.append("=== 评论（按时间，最多").append(MAX_COMMENT_COUNT).append("条）===\n");
        if (commentsJoined.isBlank()) {
            sb.append("（无有效评论）\n");
        } else {
            sb.append(commentsJoined).append('\n');
        }
        sb.append("\n请按上述结构输出，控制在300-600字，最后包含“分析师观点与建议”。");
        return sb.toString();
    }

    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    private static String sanitize(String s) {
        if (s == null) return "";
        String t = s.replaceAll("[\r\n]+", " ");
        return t.replaceAll("\\s+", " ").trim();
    }
}
