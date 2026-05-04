package org.xhy.community.application.transcript.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.xhy.community.application.permission.service.UserPermissionAppService;
import org.xhy.community.application.transcript.dto.AdminChapterTranscriptDTO;
import org.xhy.community.application.transcript.dto.ChapterTranscriptDTO;
import org.xhy.community.application.transcript.dto.ChapterTranscriptSectionDTO;
import org.xhy.community.application.transcript.dto.ChapterTranscriptSegmentDTO;
import org.xhy.community.domain.course.entity.ChapterEntity;
import org.xhy.community.domain.course.entity.CourseEntity;
import org.xhy.community.domain.course.service.ChapterDomainService;
import org.xhy.community.domain.course.service.CourseDomainService;
import org.xhy.community.domain.resource.entity.ResourceEntity;
import org.xhy.community.domain.resource.service.ResourceDomainService;
import org.xhy.community.domain.resource.valueobject.ResourceType;
import org.xhy.community.domain.transcript.entity.ChapterTranscriptEntity;
import org.xhy.community.domain.transcript.entity.ChapterTranscriptSegmentEntity;
import org.xhy.community.domain.transcript.service.ChapterTranscriptDomainService;
import org.xhy.community.infrastructure.ai.ChatAIClient;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.CourseErrorCode;
import org.xhy.community.infrastructure.markdown.MarkdownParser;
import org.xhy.community.infrastructure.markdown.model.MarkdownNode;
import org.xhy.community.infrastructure.markdown.model.NodeType;
import org.xhy.community.infrastructure.transcript.TranscriptProperties;
import org.xhy.community.infrastructure.transcript.TranscriptProvider;
import org.xhy.community.infrastructure.transcript.TranscriptSubmitCommand;
import org.xhy.community.infrastructure.transcript.TranscriptSubmitResult;
import org.xhy.community.infrastructure.transcript.TranscriptTaskResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChapterTranscriptAppService {

    private static final String STATUS_NOT_GENERATED = "NOT_GENERATED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_RUNNING = "RUNNING";
    private static final String STATUS_SUCCEEDED = "SUCCEEDED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final Pattern RESOURCE_URL_PATTERN = Pattern.compile("/api/public/resource/([^/]+)/access");
    private static final int AI_TIMELINE_INPUT_LIMIT = 22000;

    private final ChapterTranscriptDomainService transcriptDomainService;
    private final ChapterDomainService chapterDomainService;
    private final CourseDomainService courseDomainService;
    private final ResourceDomainService resourceDomainService;
    private final UserPermissionAppService userPermissionAppService;
    private final MarkdownParser markdownParser;
    private final TranscriptProvider transcriptProvider;
    private final TranscriptProperties properties;
    private final ChatAIClient chatAIClient;
    private final ObjectMapper objectMapper;

    public ChapterTranscriptAppService(ChapterTranscriptDomainService transcriptDomainService,
                                       ChapterDomainService chapterDomainService,
                                       CourseDomainService courseDomainService,
                                       ResourceDomainService resourceDomainService,
                                       UserPermissionAppService userPermissionAppService,
                                       MarkdownParser markdownParser,
                                       TranscriptProvider transcriptProvider,
                                       TranscriptProperties properties,
                                       ChatAIClient chatAIClient,
                                       ObjectMapper objectMapper) {
        this.transcriptDomainService = transcriptDomainService;
        this.chapterDomainService = chapterDomainService;
        this.courseDomainService = courseDomainService;
        this.resourceDomainService = resourceDomainService;
        this.userPermissionAppService = userPermissionAppService;
        this.markdownParser = markdownParser;
        this.transcriptProvider = transcriptProvider;
        this.properties = properties;
        this.chatAIClient = chatAIClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createPendingTaskForChapter(ChapterEntity chapter) {
        if (!Boolean.TRUE.equals(properties.getEnabled()) || chapter == null) {
            return;
        }
        String resourceId = extractFirstVideoResourceId(chapter.getContent());
        if (!StringUtils.hasText(resourceId) || !isAudioOrVideoResource(resourceId)) {
            return;
        }

        ChapterTranscriptEntity same = transcriptDomainService.getActiveByChapterAndResource(chapter.getId(), resourceId);
        if (same != null) {
            return;
        }

        ChapterTranscriptEntity current = transcriptDomainService.getActiveByChapterId(chapter.getId());
        if (current != null) {
            transcriptDomainService.softDeleteTranscriptAndSegments(current.getId());
        }

        ChapterTranscriptEntity transcript = new ChapterTranscriptEntity();
        transcript.setCourseId(chapter.getCourseId());
        transcript.setChapterId(chapter.getId());
        transcript.setResourceId(resourceId);
        transcript.setProvider(properties.getProvider());
        transcript.setModel(properties.getModel());
        transcript.setLanguage(properties.getLanguage());
        transcript.setStatus(STATUS_PENDING);
        transcriptDomainService.create(transcript);
    }

    public ChapterTranscriptDTO getAppTranscript(String chapterId, String userId) {
        ChapterEntity chapter = chapterDomainService.getChapterById(chapterId);
        validateChapterAccess(chapter.getCourseId(), userId);
        ChapterTranscriptEntity transcript = transcriptDomainService.getActiveByChapterId(chapterId);
        if (transcript == null) {
            return notGenerated(chapterId);
        }
        return toAppDTO(transcript, transcriptDomainService.listSegments(transcript.getId()));
    }

    public AdminChapterTranscriptDTO getAdminTranscript(String chapterId) {
        ChapterTranscriptEntity transcript = transcriptDomainService.getActiveByChapterId(chapterId);
        if (transcript == null) {
            return notGeneratedAdmin(chapterId);
        }
        return toAdminDTO(transcript);
    }

    public Map<String, AdminChapterTranscriptDTO> getAdminTranscriptMap(Collection<String> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) {
            return Map.of();
        }
        List<String> normalizedChapterIds = chapterIds.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (normalizedChapterIds.isEmpty()) {
            return Map.of();
        }
        Map<String, ChapterTranscriptEntity> transcriptMap = transcriptDomainService.getLatestActiveMapByChapterIds(normalizedChapterIds);
        Map<String, AdminChapterTranscriptDTO> result = new LinkedHashMap<>();
        for (String chapterId : normalizedChapterIds) {
            ChapterTranscriptEntity transcript = transcriptMap.get(chapterId);
            result.put(chapterId, transcript == null ? notGeneratedAdmin(chapterId) : toAdminDTO(transcript));
        }
        return result;
    }

    @Transactional
    public AdminChapterTranscriptDTO retry(String chapterId) {
        ChapterTranscriptEntity transcript = transcriptDomainService.getActiveByChapterId(chapterId);
        if (transcript == null) {
            ChapterEntity chapter = chapterDomainService.getChapterById(chapterId);
            createPendingTaskForChapter(chapter);
            transcript = transcriptDomainService.getActiveByChapterId(chapterId);
        }
        if (transcript == null) {
            return notGeneratedAdmin(chapterId);
        }
        submit(transcript);
        return toAdminDTO(transcript);
    }

    @Transactional
    public AdminChapterTranscriptDTO regenerate(String chapterId) {
        ChapterTranscriptEntity current = transcriptDomainService.getActiveByChapterId(chapterId);
        if (current != null) {
            transcriptDomainService.softDeleteTranscriptAndSegments(current.getId());
        }
        ChapterEntity chapter = chapterDomainService.getChapterById(chapterId);
        createPendingTaskForChapter(chapter);
        ChapterTranscriptEntity created = transcriptDomainService.getActiveByChapterId(chapterId);
        if (created == null) {
            return notGeneratedAdmin(chapterId);
        }
        submit(created);
        return toAdminDTO(created);
    }

    @Transactional
    public int batchGenerateByCourse(String courseId) {
        List<ChapterEntity> chapters = chapterDomainService.getChaptersByCourseId(courseId);
        int count = 0;
        for (ChapterEntity chapter : chapters) {
            ChapterTranscriptEntity before = transcriptDomainService.getActiveByChapterId(chapter.getId());
            createPendingTaskForChapter(chapter);
            ChapterTranscriptEntity after = transcriptDomainService.getActiveByChapterId(chapter.getId());
            if (after != null && (before == null || !after.getId().equals(before.getId()))) {
                count++;
            }
        }
        return count;
    }

    public void processPendingAndRunning() {
        List<ChapterTranscriptEntity> tasks = transcriptDomainService.listByStatuses(
                List.of(STATUS_PENDING, STATUS_SUBMITTED, STATUS_RUNNING),
                properties.getPollLimit() == null ? 10 : properties.getPollLimit()
        );
        for (ChapterTranscriptEntity task : tasks) {
            try {
                if (STATUS_PENDING.equals(task.getStatus())) {
                    submit(task);
                } else {
                    refreshProviderResult(task);
                }
            } catch (Exception e) {
                task.setStatus(STATUS_FAILED);
                task.setErrorCode("TRANSCRIPT_PROCESS_FAILED");
                task.setErrorMessage(limit(e.getMessage(), 1000));
                transcriptDomainService.update(task);
            }
        }
    }

    private void submit(ChapterTranscriptEntity transcript) {
        try {
            String fileUrl = resourceDomainService.getProviderDownloadUrl(
                    transcript.getResourceId(),
                    properties.getProviderFileUrlExpirationSeconds()
            );
            TranscriptSubmitCommand command = new TranscriptSubmitCommand();
            command.setModel(transcript.getModel());
            command.setFileUrl(fileUrl);
            command.setLanguage(transcript.getLanguage());
            command.setEnableItn(properties.getEnableItn());
            command.setEnableWords(properties.getEnableWords());

            TranscriptSubmitResult result = transcriptProvider.submit(command);
            transcript.setProviderTaskId(result.getTaskId());
            transcript.setStatus(mapSubmittedStatus(result.getTaskStatus()));
            transcript.setRawResultJson(result.getRawJson());
            transcript.setSubmittedAt(LocalDateTime.now());
            transcript.setErrorCode(null);
            transcript.setErrorMessage(null);
            transcriptDomainService.update(transcript);
        } catch (Exception e) {
            transcript.setStatus(STATUS_FAILED);
            transcript.setErrorCode("DASHSCOPE_SUBMIT_FAILED");
            transcript.setErrorMessage(limit(e.getMessage(), 1000));
            transcriptDomainService.update(transcript);
        }
    }

    private void refreshProviderResult(ChapterTranscriptEntity transcript) {
        if (!StringUtils.hasText(transcript.getProviderTaskId())) {
            transcript.setStatus(STATUS_PENDING);
            transcriptDomainService.update(transcript);
            return;
        }

        TranscriptTaskResult result = transcriptProvider.query(transcript.getProviderTaskId());
        String status = result.getTaskStatus();
        if ("SUCCEEDED".equals(status)) {
            completeTranscript(transcript, result);
            return;
        }
        if ("FAILED".equals(status) || "UNKNOWN".equals(status)) {
            transcript.setStatus(STATUS_FAILED);
            transcript.setErrorCode(StringUtils.hasText(result.getErrorCode()) ? result.getErrorCode() : status);
            transcript.setErrorMessage(limit(result.getErrorMessage(), 1000));
            transcript.setRawResultJson(result.getRawJson());
            transcript.setCompletedAt(LocalDateTime.now());
            transcriptDomainService.update(transcript);
            return;
        }
        transcript.setStatus("RUNNING".equals(status) ? STATUS_RUNNING : STATUS_SUBMITTED);
        transcript.setRawResultJson(result.getRawJson());
        transcriptDomainService.update(transcript);
    }

    private void completeTranscript(ChapterTranscriptEntity transcript, TranscriptTaskResult result) {
        transcript.setStatus(STATUS_SUCCEEDED);
        transcript.setText(result.getText());
        transcript.setDurationMs(result.getDurationMs());
        transcript.setRawResultJson(StringUtils.hasText(result.getResultJson()) ? result.getResultJson() : result.getRawJson());
        transcript.setErrorCode(null);
        transcript.setErrorMessage(null);
        transcript.setCompletedAt(LocalDateTime.now());

        List<ChapterTranscriptSegmentEntity> segments = new ArrayList<>();
        int index = 0;
        for (TranscriptTaskResult.Segment source : result.getSegments()) {
            ChapterTranscriptSegmentEntity segment = new ChapterTranscriptSegmentEntity();
            segment.setTranscriptId(transcript.getId());
            segment.setCourseId(transcript.getCourseId());
            segment.setChapterId(transcript.getChapterId());
            segment.setStartMs(source.getStartMs());
            segment.setEndMs(source.getEndMs());
            segment.setSpeaker(source.getSpeaker());
            segment.setText(source.getText());
            segment.setSortOrder(index++);
            segments.add(segment);
        }
        transcriptDomainService.replaceSegments(transcript.getId(), segments);
        fillAiRepresentative(transcript, segments);
        transcriptDomainService.update(transcript);
    }

    private void fillAiRepresentative(ChapterTranscriptEntity transcript, List<ChapterTranscriptSegmentEntity> segments) {
        if (!StringUtils.hasText(transcript.getText())) {
            return;
        }
        try {
            ChapterEntity chapter = chapterDomainService.getChapterById(transcript.getChapterId());
            CourseEntity course = courseDomainService.getCourseById(transcript.getCourseId());
            String response = chatAIClient.chat(
                    buildAiRepresentativeSystemPrompt(),
                    buildAiRepresentativeUserPrompt(course, chapter, segments)
            );
            JsonNode root = readAiJson(response);
            transcript.setSummary(root.path("summary").asText(response));
            if (root.has("keyPoints")) {
                transcript.setKeyPointsJson(objectMapper.writeValueAsString(objectMapper.convertValue(
                        root.path("keyPoints"),
                        new TypeReference<List<String>>() {}
                )));
            }
            if (root.has("sections")) {
                transcript.setOutlineJson(objectMapper.writeValueAsString(objectMapper.convertValue(
                        root.path("sections"),
                        new TypeReference<List<ChapterTranscriptSectionDTO>>() {}
                )));
            }
        } catch (Exception e) {
            transcript.setSummary(limit(e.getMessage(), 1000));
        }
    }

    private String buildAiRepresentativeSystemPrompt() {
        return """
                你是敲鸭社区的资深课程助教，负责把课程视频的 ASR 逐句时间轴整理成适合学习的结构化笔记。

                任务目标：
                1. 生成一段课程总结，帮助学员快速判断本节课讲了什么。
                2. 提炼关键知识点，突出技术概念、实现步骤、注意事项和结论。
                3. 把逐句文字稿合并成少量“大模块时间段”，让页面默认展示更像课程目录，而不是一两句话一个时间点。

                处理规则：
                - 只能依据输入的课程标题、章节标题和带时间的文字稿，不要补充输入外事实。
                - 如果 ASR 有错别字，可在不改变含义的前提下轻微润色。
                - 大模块应按主题连续合并，通常 5 到 12 个；短视频可以更少，长视频可以略多。
                - 每个模块必须使用输入中已有时间附近的 startMs 和 endMs，不要编造超出输入范围的时间。
                - startMs/endMs 使用毫秒整数，必须递增，模块之间不要重叠。
                - title 要像课程小节标题，简短具体，不要写“第一部分”“第二部分”这类空泛标题。
                - summary 用 1 到 2 句说明该模块讲了什么。
                - text 是该模块内容的连贯整理，不要逐句罗列。

                输出要求：
                - 只输出 JSON 对象，不要 Markdown，不要代码块，不要解释。
                - JSON 字段固定为：
                  {
                    "summary": "180到350字中文总结",
                    "keyPoints": ["6到12条关键知识点"],
                    "sections": [
                      {
                        "title": "模块标题",
                        "startMs": 0,
                        "endMs": 180000,
                        "summary": "模块摘要",
                        "text": "模块整理后的正文",
                        "sortOrder": 0
                      }
                    ]
                  }
                """;
    }

    private String buildAiRepresentativeUserPrompt(CourseEntity course,
                                                   ChapterEntity chapter,
                                                   List<ChapterTranscriptSegmentEntity> segments) {
        StringBuilder timeline = new StringBuilder();
        if (segments != null) {
            for (ChapterTranscriptSegmentEntity segment : segments) {
                if (!StringUtils.hasText(segment.getText())) {
                    continue;
                }
                timeline.append('[')
                        .append(segment.getStartMs() == null ? 0 : segment.getStartMs())
                        .append('-')
                        .append(segment.getEndMs() == null ? "" : segment.getEndMs())
                        .append("] ")
                        .append(segment.getText().replaceAll("[\\r\\n]+", " ").trim())
                        .append('\n');
                if (timeline.length() >= AI_TIMELINE_INPUT_LIMIT) {
                    timeline.append("\n[内容因长度限制已截断，请只依据以上时间轴输出]\n");
                    break;
                }
            }
        }
        return """
                <course_title>
                %s
                </course_title>

                <chapter_title>
                %s
                </chapter_title>

                <timed_transcript>
                %s
                </timed_transcript>
                """.formatted(
                nullToEmpty(course.getTitle()),
                nullToEmpty(chapter.getTitle()),
                timeline.toString()
        );
    }

    private JsonNode readAiJson(String response) throws Exception {
        String text = response == null ? "{}" : response.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```json\\s*", "").replaceFirst("^```\\s*", "");
            int end = text.lastIndexOf("```");
            if (end >= 0) {
                text = text.substring(0, end);
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1);
        }
        return objectMapper.readTree(text);
    }

    private String extractFirstVideoResourceId(String markdown) {
        if (!StringUtils.hasText(markdown)) {
            return null;
        }
        MarkdownNode root = markdownParser.parse(markdown);
        Set<String> ids = new LinkedHashSet<>();
        collectVideoResourceIds(root, ids);
        return ids.stream().findFirst().orElse(null);
    }

    private void collectVideoResourceIds(MarkdownNode node, Set<String> out) {
        if (node == null) {
            return;
        }
        if (node.getType() == NodeType.VIDEO) {
            String src = node.getAttributes().get("src");
            String id = extractResourceIdFromUrl(src);
            if (id != null) {
                out.add(id);
            }
        }
        if (node.getChildren() != null) {
            for (MarkdownNode child : node.getChildren()) {
                collectVideoResourceIds(child, out);
            }
        }
    }

    private String extractResourceIdFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        Matcher matcher = RESOURCE_URL_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean isAudioOrVideoResource(String resourceId) {
        try {
            ResourceEntity resource = resourceDomainService.getResourceById(resourceId);
            return resource.getResourceType() == ResourceType.VIDEO || resource.getResourceType() == ResourceType.AUDIO;
        } catch (Exception e) {
            return false;
        }
    }

    private String mapSubmittedStatus(String providerStatus) {
        if ("RUNNING".equals(providerStatus)) {
            return STATUS_RUNNING;
        }
        if ("SUCCEEDED".equals(providerStatus)) {
            return STATUS_RUNNING;
        }
        if ("FAILED".equals(providerStatus) || "UNKNOWN".equals(providerStatus)) {
            return STATUS_FAILED;
        }
        return STATUS_SUBMITTED;
    }

    private ChapterTranscriptDTO toAppDTO(ChapterTranscriptEntity transcript, List<ChapterTranscriptSegmentEntity> segments) {
        ChapterTranscriptDTO dto = new ChapterTranscriptDTO();
        dto.setChapterId(transcript.getChapterId());
        dto.setResourceId(transcript.getResourceId());
        dto.setStatus(transcript.getStatus());
        dto.setDurationMs(transcript.getDurationMs());
        dto.setText(transcript.getText());
        dto.setSummary(transcript.getSummary());
        dto.setKeyPoints(parseKeyPoints(transcript.getKeyPointsJson()));
        dto.setSections(parseSections(transcript.getOutlineJson()));
        dto.setCompletedAt(transcript.getCompletedAt());
        dto.setSegments(segments.stream().map(this::toSegmentDTO).toList());
        return dto;
    }

    private ChapterTranscriptSegmentDTO toSegmentDTO(ChapterTranscriptSegmentEntity entity) {
        ChapterTranscriptSegmentDTO dto = new ChapterTranscriptSegmentDTO();
        dto.setStartMs(entity.getStartMs());
        dto.setEndMs(entity.getEndMs());
        dto.setSpeaker(entity.getSpeaker());
        dto.setText(entity.getText());
        dto.setSortOrder(entity.getSortOrder());
        return dto;
    }

    private AdminChapterTranscriptDTO toAdminDTO(ChapterTranscriptEntity transcript) {
        AdminChapterTranscriptDTO dto = new AdminChapterTranscriptDTO();
        dto.setChapterId(transcript.getChapterId());
        dto.setResourceId(transcript.getResourceId());
        dto.setStatus(transcript.getStatus());
        dto.setProvider(transcript.getProvider());
        dto.setModel(transcript.getModel());
        dto.setProviderTaskId(transcript.getProviderTaskId());
        dto.setDurationMs(transcript.getDurationMs());
        dto.setEstimatedCost(estimateCost(transcript.getDurationMs()));
        dto.setErrorCode(transcript.getErrorCode());
        dto.setErrorMessage(transcript.getErrorMessage());
        dto.setSubmittedAt(transcript.getSubmittedAt());
        dto.setCompletedAt(transcript.getCompletedAt());
        return dto;
    }

    private ChapterTranscriptDTO notGenerated(String chapterId) {
        ChapterTranscriptDTO dto = new ChapterTranscriptDTO();
        dto.setChapterId(chapterId);
        dto.setStatus(STATUS_NOT_GENERATED);
        dto.setSegments(List.of());
        dto.setKeyPoints(List.of());
        dto.setSections(List.of());
        return dto;
    }

    private AdminChapterTranscriptDTO notGeneratedAdmin(String chapterId) {
        AdminChapterTranscriptDTO dto = new AdminChapterTranscriptDTO();
        dto.setChapterId(chapterId);
        dto.setStatus(STATUS_NOT_GENERATED);
        return dto;
    }

    private List<String> parseKeyPoints(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<ChapterTranscriptSectionDTO> parseSections(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ChapterTranscriptSectionDTO>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    private BigDecimal estimateCost(Long durationMs) {
        if (durationMs == null || properties.getPricePerSecond() == null) {
            return null;
        }
        BigDecimal seconds = BigDecimal.valueOf(durationMs).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
        return seconds.multiply(properties.getPricePerSecond()).setScale(4, RoundingMode.HALF_UP);
    }

    private void validateChapterAccess(String courseId, String userId) {
        if (userId == null || !userPermissionAppService.hasAccessToCourse(userId, courseId)) {
            throw new BusinessException(CourseErrorCode.CHAPTER_ACCESS_DENIED);
        }
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
