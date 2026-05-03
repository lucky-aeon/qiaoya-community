package org.xhy.community.infrastructure.transcript;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
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
        ensureApiKey();

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", command.getModel());
        payload.put("input", Map.of("file_url", command.getFileUrl()));

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("channel_id", List.of(0));
        parameters.put("enable_itn", Boolean.TRUE.equals(command.getEnableItn()));
        parameters.put("enable_words", Boolean.TRUE.equals(command.getEnableWords()));
        if (StringUtils.hasText(command.getLanguage())) {
            parameters.put("language", command.getLanguage());
        }
        payload.put("parameters", parameters);

        String body = exchange(properties.getDashscope().getSubmitUrl(), HttpMethod.POST, payload);
        JsonNode root = readTree(body);
        JsonNode output = root.path("output");

        TranscriptSubmitResult result = new TranscriptSubmitResult();
        result.setTaskId(output.path("task_id").asText(null));
        result.setTaskStatus(output.path("task_status").asText(null));
        result.setRawJson(body);
        return result;
    }

    @Override
    public TranscriptTaskResult query(String providerTaskId) {
        ensureApiKey();

        String url = properties.getDashscope().getTaskUrlPrefix() + providerTaskId;
        String body = exchange(url, HttpMethod.GET, null);
        JsonNode root = readTree(body);
        JsonNode output = root.path("output");

        TranscriptTaskResult result = new TranscriptTaskResult();
        result.setTaskStatus(output.path("task_status").asText(null));
        result.setRawJson(body);
        result.setErrorCode(output.path("code").asText(null));
        result.setErrorMessage(output.path("message").asText(null));
        if (root.has("usage") && root.path("usage").has("seconds")) {
            result.setDurationMs(root.path("usage").path("seconds").asLong() * 1000);
        }

        String transcriptionUrl = output.path("result").path("transcription_url").asText(null);
        result.setTranscriptionUrl(transcriptionUrl);
        if ("SUCCEEDED".equals(result.getTaskStatus()) && StringUtils.hasText(transcriptionUrl)) {
            String resultJson = restTemplate.getForObject(URI.create(transcriptionUrl), String.class);
            result.setResultJson(resultJson);
            fillTranscriptResult(result, resultJson);
        }
        return result;
    }

    private void fillTranscriptResult(TranscriptTaskResult result, String resultJson) {
        JsonNode root = readTree(resultJson);
        List<TranscriptTaskResult.Segment> segments = new ArrayList<>();
        List<String> texts = new ArrayList<>();
        long maxEndMs = 0L;

        JsonNode transcripts = root.path("transcripts");
        if (transcripts.isArray()) {
            for (JsonNode transcript : transcripts) {
                String transcriptText = transcript.path("text").asText("");
                if (StringUtils.hasText(transcriptText)) {
                    texts.add(transcriptText);
                }
                String speaker = transcript.has("channel_id") ? "channel-" + transcript.path("channel_id").asInt() : null;
                JsonNode sentences = transcript.path("sentences");
                if (sentences.isArray()) {
                    for (JsonNode sentence : sentences) {
                        String text = sentence.path("text").asText("");
                        if (!StringUtils.hasText(text)) {
                            continue;
                        }
                        TranscriptTaskResult.Segment segment = new TranscriptTaskResult.Segment();
                        segment.setStartMs(sentence.has("begin_time") ? sentence.path("begin_time").asLong() : null);
                        segment.setEndMs(sentence.has("end_time") ? sentence.path("end_time").asLong() : null);
                        segment.setSpeaker(speaker);
                        segment.setText(text);
                        segments.add(segment);
                        if (segment.getEndMs() != null) {
                            maxEndMs = Math.max(maxEndMs, segment.getEndMs());
                        }
                    }
                }
            }
        }

        result.setText(String.join("\n", texts));
        result.setSegments(segments);
        if (result.getDurationMs() == null && maxEndMs > 0) {
            result.setDurationMs(maxEndMs);
        }
    }

    private String exchange(String url, HttpMethod method, Object payload) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(properties.getDashscope().getApiKey().trim());
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-DashScope-Async", "enable");

            Object body = payload == null ? null : objectMapper.writeValueAsString(payload);
            return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), String.class).getBody();
        } catch (RestClientResponseException e) {
            throw new IllegalStateException("DashScope API 调用失败：" + e.getStatusCode() + " " + safeError(e.getResponseBodyAsString()), e);
        } catch (Exception e) {
            throw new IllegalStateException("DashScope API 调用失败：" + e.getMessage(), e);
        }
    }

    private String safeError(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "";
        }
        String compact = responseBody.replaceAll("\\s+", " ").trim();
        return compact.length() > 300 ? compact.substring(0, 300) : compact;
    }

    private JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("解析 DashScope 响应失败", e);
        }
    }

    private void ensureApiKey() {
        if (!StringUtils.hasText(properties.getDashscope().getApiKey())) {
            throw new IllegalStateException("未配置 DASHSCOPE_API_KEY");
        }
    }
}
