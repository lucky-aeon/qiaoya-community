package org.xhy.community.infrastructure.transcript;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "transcript")
public class TranscriptProperties {

    private Boolean enabled = true;
    private String provider = "DASHSCOPE_QWEN_ASR";
    private String model = "qwen3-asr-flash-filetrans";
    private String language = "zh";
    private Boolean enableWords = true;
    private Boolean enableItn = false;
    private Integer pollLimit = 10;
    private Long providerFileUrlExpirationSeconds = 172800L;
    private BigDecimal pricePerSecond = new BigDecimal("0.00022");
    private Dashscope dashscope = new Dashscope();

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Boolean getEnableWords() { return enableWords; }
    public void setEnableWords(Boolean enableWords) { this.enableWords = enableWords; }

    public Boolean getEnableItn() { return enableItn; }
    public void setEnableItn(Boolean enableItn) { this.enableItn = enableItn; }

    public Integer getPollLimit() { return pollLimit; }
    public void setPollLimit(Integer pollLimit) { this.pollLimit = pollLimit; }

    public Long getProviderFileUrlExpirationSeconds() { return providerFileUrlExpirationSeconds; }
    public void setProviderFileUrlExpirationSeconds(Long providerFileUrlExpirationSeconds) { this.providerFileUrlExpirationSeconds = providerFileUrlExpirationSeconds; }

    public BigDecimal getPricePerSecond() { return pricePerSecond; }
    public void setPricePerSecond(BigDecimal pricePerSecond) { this.pricePerSecond = pricePerSecond; }

    public Dashscope getDashscope() { return dashscope; }
    public void setDashscope(Dashscope dashscope) { this.dashscope = dashscope; }

    public static class Dashscope {
        private String apiKey;
        private String submitUrl = "https://dashscope.aliyuncs.com/api/v1/services/audio/asr/transcription";
        private String taskUrlPrefix = "https://dashscope.aliyuncs.com/api/v1/tasks/";

        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey == null ? null : apiKey.trim(); }

        public String getSubmitUrl() { return submitUrl; }
        public void setSubmitUrl(String submitUrl) { this.submitUrl = submitUrl; }

        public String getTaskUrlPrefix() { return taskUrlPrefix; }
        public void setTaskUrlPrefix(String taskUrlPrefix) { this.taskUrlPrefix = taskUrlPrefix; }
    }
}
