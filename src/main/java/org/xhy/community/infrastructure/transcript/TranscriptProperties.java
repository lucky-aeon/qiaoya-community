package org.xhy.community.infrastructure.transcript;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "transcript")
public class TranscriptProperties {

    private boolean enabled = false;
    private String provider = "dashscope-qwen-asr";
    private String model = "qwen3-asr-flash-filetrans";
    private String language = "zh";
    private long fileUrlExpirationSeconds = 43200;
    private Poll poll = new Poll();
    private Dashscope dashscope = new Dashscope();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public long getFileUrlExpirationSeconds() { return fileUrlExpirationSeconds; }
    public void setFileUrlExpirationSeconds(long fileUrlExpirationSeconds) { this.fileUrlExpirationSeconds = fileUrlExpirationSeconds; }

    public Poll getPoll() { return poll; }
    public void setPoll(Poll poll) { this.poll = poll; }

    public Dashscope getDashscope() { return dashscope; }
    public void setDashscope(Dashscope dashscope) { this.dashscope = dashscope; }

    public static class Poll {
        private long fixedDelayMs = 60000;
        private int submitLimit = 5;
        private int queryLimit = 10;

        public long getFixedDelayMs() { return fixedDelayMs; }
        public void setFixedDelayMs(long fixedDelayMs) { this.fixedDelayMs = fixedDelayMs; }

        public int getSubmitLimit() { return submitLimit; }
        public void setSubmitLimit(int submitLimit) { this.submitLimit = submitLimit; }

        public int getQueryLimit() { return queryLimit; }
        public void setQueryLimit(int queryLimit) { this.queryLimit = queryLimit; }
    }

    public static class Dashscope {
        private String apiKey;
        private String baseUrl = "https://dashscope.aliyuncs.com";

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }
}
