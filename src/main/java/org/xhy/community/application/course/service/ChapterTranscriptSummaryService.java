package org.xhy.community.application.course.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.application.course.transcript.TranscriptSegmentResult;
import org.xhy.community.infrastructure.ai.ChatAIClient;

import java.util.List;

@Service
public class ChapterTranscriptSummaryService {

    private static final Logger log = LoggerFactory.getLogger(ChapterTranscriptSummaryService.class);
    private static final int MAX_TRANSCRIPT_CHARS = 16000;
    private static final String SYSTEM_PROMPT = """
            你是敲鸭社区的课程 AI 课代表，负责把课程视频文字稿整理成便于学员复习的内容。

            要求：
            - 只依据用户提供的课程标题和文字稿，不编造材料外信息。
            - summary 用 150-300 字概括本节核心内容。
            - keyPoints 提取 4-8 个知识点，每个知识点保持一句话，强调可操作、可复习。
            - 输出严格 JSON，不要 Markdown，不要代码块，不要额外解释。

            JSON 格式：
            {"summary":"...","keyPoints":["...","..."]}
            """;

    private final ChatAIClient chatAIClient;
    private final ObjectMapper objectMapper;

    public ChapterTranscriptSummaryService(ChatAIClient chatAIClient, ObjectMapper objectMapper) {
        this.chatAIClient = chatAIClient;
        this.objectMapper = objectMapper;
    }

    public SummaryResult summarize(String chapterTitle, String transcriptText, List<TranscriptSegmentResult> segments) {
        SummaryResult result = new SummaryResult();
        if (!StringUtils.hasText(transcriptText)) {
            result.setSummary("");
            result.setKeyPoints(List.of());
            return result;
        }

        try {
            String aiResult = chatAIClient.chat(SYSTEM_PROMPT, buildUserPrompt(chapterTitle, transcriptText));
            JsonNode root = objectMapper.readTree(stripCodeFence(aiResult));
            result.setSummary(root.path("summary").asText(""));
            result.setKeyPoints(parseKeyPoints(root.path("keyPoints")));
        } catch (Exception e) {
            log.warn("【章节转写】AI 总结生成失败：chapterTitle={}, error={}", chapterTitle, e.getMessage());
            result.setSummary("");
            result.setKeyPoints(List.of());
        }
        return result;
    }

    private String buildUserPrompt(String chapterTitle, String transcriptText) {
        String text = transcriptText.trim();
        boolean truncated = text.length() > MAX_TRANSCRIPT_CHARS;
        String effectiveText = truncated ? text.substring(0, MAX_TRANSCRIPT_CHARS) : text;

        return """
                请为以下课程视频文字稿生成 AI 总结和知识点。

                标题：%s
                文字稿%s：
                %s
                """.formatted(
                StringUtils.hasText(chapterTitle) ? chapterTitle.trim() : "未命名章节",
                truncated ? "（因长度限制已截取前半部分）" : "",
                effectiveText
        );
    }

    private List<String> parseKeyPoints(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        java.util.ArrayList<String> keyPoints = new java.util.ArrayList<>();
        for (JsonNode item : node) {
            String value = item.asText("");
            if (StringUtils.hasText(value)) {
                keyPoints.add(value.trim());
            }
        }
        return keyPoints;
    }

    private String stripCodeFence(String value) {
        if (value == null) {
            return "{}";
        }
        String text = value.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```(?:json)?\\s*", "");
            text = text.replaceFirst("\\s*```$", "");
        }
        return text.trim();
    }

    public static class SummaryResult {
        private String summary;
        private List<String> keyPoints = List.of();

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public List<String> getKeyPoints() { return keyPoints; }
        public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }
    }
}
