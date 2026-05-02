package org.xhy.community.domain.course.valueobject;

public enum ChapterTranscriptProvider {
    DASHSCOPE_QWEN_ASR("百炼 Qwen ASR"),
    DASHSCOPE_FUN_ASR("百炼 Fun-ASR"),
    TINGWU("通义听悟"),
    NOOP("未配置真实转写服务");

    private final String description;

    ChapterTranscriptProvider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static ChapterTranscriptProvider fromCode(String code) {
        for (ChapterTranscriptProvider provider : values()) {
            if (provider.name().equals(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown chapter transcript provider: " + code);
    }
}
