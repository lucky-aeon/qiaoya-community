package org.xhy.community.infrastructure.transcript;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.xhy.community.application.course.transcript.TranscriptProvider;
import org.xhy.community.application.course.transcript.TranscriptProviderTaskResult;
import org.xhy.community.application.course.transcript.TranscriptSegmentResult;
import org.xhy.community.application.course.transcript.TranscriptSubmitCommand;
import org.xhy.community.application.course.transcript.TranscriptSubmitResult;
import org.xhy.community.domain.course.valueobject.ChapterTranscriptStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;

@Component
@ConditionalOnExpression("'${transcript.provider:dashscope-qwen-asr}'.equalsIgnoreCase('dashscope-qwen-asr') || '${transcript.provider:dashscope-qwen-asr}'.equalsIgnoreCase('bailian')")
public class DashScopeQwenAsrTranscriptProvider implements TranscriptProvider {

    private final TranscriptProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    public DashScopeQwenAsrTranscriptProvider(TranscriptProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public TranscriptSubmitResult submit(TranscriptSubmitCommand command) {
        requireApiKey();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", StringUtils.hasText(command.getModel()) ? command.getModel() : properties.getModel());
        payload.put("input", Map.of("file_url", command.getFileUrl()));

        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("channel_id", List.of(0));
        parameters.put("enable_itn", false);
        parameters.put("enable_words", true);
        if (StringUtils.hasText(command.getLanguage())) {
            parameters.put("language", command.getLanguage());
        }
        payload.put("parameters", parameters);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                apiBase() + "/api/v1/services/audio/asr/transcription",
                HttpMethod.POST,
                new HttpEntity<>(payload, asyncHeaders()),
                JsonNode.class
        );

        JsonNode body = response.getBody();
        JsonNode output = body == null ? null : body.path("output");
        String taskId = output == null ? null : output.path("task_id").asText(null);
        if (!StringUtils.hasText(taskId)) {
            throw new IllegalStateException("DashScope 未返回 task_id");
        }
        return new TranscriptSubmitResult(taskId, mapStatus(output.path("task_status").asText("SUBMITTED")));
    }

    @Override
    public TranscriptProviderTaskResult query(String providerTaskId) {
        requireApiKey();

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                apiBase() + "/api/v1/tasks/" + providerTaskId,
                HttpMethod.GET,
                new HttpEntity<>(jsonHeaders()),
                JsonNode.class
        );

        JsonNode body = response.getBody();
        JsonNode output = body == null ? objectMapper.createObjectNode() : body.path("output");

        TranscriptProviderTaskResult result = new TranscriptProviderTaskResult();
        ChapterTranscriptStatus status = mapStatus(output.path("task_status").asText(null));
        result.setStatus(status);

        if (status == ChapterTranscriptStatus.FAILED) {
            result.setErrorCode(output.path("code").asText("DASHSCOPE_FAILED"));
            result.setErrorMessage(output.path("message").asText("DashScope 转写任务失败"));
            result.setRawResultJson(toJson(body));
            return result;
        }

        if (status != ChapterTranscriptStatus.SUCCEEDED) {
            result.setRawResultJson(toJson(body));
            return result;
        }

        JsonNode usageSeconds = body == null ? null : body.path("usage").path("seconds");
        if (usageSeconds != null && usageSeconds.isNumber()) {
            result.setDurationMs(usageSeconds.asLong() * 1000L);
        }

        String transcriptUrl = output.path("result").path("transcription_url").asText(null);
        if (!StringUtils.hasText(transcriptUrl)) {
            result.setRawResultJson(toJson(body));
            return result;
        }

        JsonNode transcriptJson = downloadTranscript(transcriptUrl);
        ParsedTranscript parsed = parseTranscript(transcriptJson);
        result.setText(parsed.text());
        result.setSegments(parsed.segments());
        result.setDurationMs(result.getDurationMs() != null ? result.getDurationMs() : parsed.durationMs());
        result.setRawResultJson(toJson(transcriptJson));
        return result;
    }

    private JsonNode downloadTranscript(String url) {
        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(URI.create(url), HttpMethod.GET, HttpEntity.EMPTY, JsonNode.class);
            return response.getBody() == null ? objectMapper.createObjectNode() : response.getBody();
        } catch (RestClientException e) {
            throw new IllegalStateException("下载 DashScope 转写结果失败: " + e.getMessage(), e);
        }
    }

    private ParsedTranscript parseTranscript(JsonNode root) {
        List<String> texts = new ArrayList<>();
        List<TranscriptSegmentResult> segments = new ArrayList<>();
        long maxEnd = 0L;
        int order = 0;

        JsonNode transcripts = root.path("transcripts");
        if (transcripts.isArray()) {
            for (JsonNode transcript : transcripts) {
                String text = transcript.path("text").asText("");
                if (StringUtils.hasText(text)) {
                    texts.add(text);
                }
                JsonNode sentences = transcript.path("sentences");
                if (sentences.isArray()) {
                    for (JsonNode sentence : sentences) {
                        String sentenceText = sentence.path("text").asText("");
                        if (!StringUtils.hasText(sentenceText)) {
                            continue;
                        }
                        long begin = sentence.path("begin_time").asLong(0L);
                        long end = sentence.path("end_time").asLong(begin);
                        maxEnd = Math.max(maxEnd, end);
                        segments.add(new TranscriptSegmentResult(begin, end, null, sentenceText, order++));
                    }
                }
            }
        }

        return new ParsedTranscript(String.join("\n", texts), segments, maxEnd > 0 ? maxEnd : null);
    }

    private HttpHeaders asyncHeaders() {
        HttpHeaders headers = jsonHeaders();
        headers.set("X-DashScope-Async", "enable");
        return headers;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getDashscope().getApiKey());
        return headers;
    }

    private void requireApiKey() {
        if (!StringUtils.hasText(properties.getDashscope().getApiKey())) {
            throw new IllegalStateException("缺少 DASHSCOPE_API_KEY");
        }
    }

    private String apiBase() {
        String base = properties.getDashscope().getBaseUrl();
        if (!StringUtils.hasText(base)) {
            base = "https://dashscope.aliyuncs.com";
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    private ChapterTranscriptStatus mapStatus(String status) {
        if ("PENDING".equalsIgnoreCase(status)) {
            return ChapterTranscriptStatus.PENDING;
        }
        if ("RUNNING".equalsIgnoreCase(status)) {
            return ChapterTranscriptStatus.RUNNING;
        }
        if ("SUCCEEDED".equalsIgnoreCase(status)) {
            return ChapterTranscriptStatus.SUCCEEDED;
        }
        if ("FAILED".equalsIgnoreCase(status) || "UNKNOWN".equalsIgnoreCase(status)) {
            return ChapterTranscriptStatus.FAILED;
        }
        return ChapterTranscriptStatus.SUBMITTED;
    }

    private String toJson(JsonNode node) {
        try {
            return node == null ? null : objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return null;
        }
    }

    private record ParsedTranscript(String text, List<TranscriptSegmentResult> segments, Long durationMs) {
    }
}
