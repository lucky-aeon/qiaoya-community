package org.xhy.community.domain.notification.valueobject;

/**
 * 批量发送配置
 */
public class BatchSendConfig {

    /** 每批发送数量，默认50 */
    private int batchSize = 50;

    /** 批次间延迟时间（毫秒），默认1秒 */
    private long delayBetweenBatches = 1000;

    /** 出错时是否跳过继续处理，默认true */
    private boolean skipOnError = true;

    /** 是否记录详细日志，默认true */
    private boolean logDetail = true;

    /** 最大重试次数，默认0（不重试） */
    private int maxRetries = 0;

    public BatchSendConfig() {
    }

    public BatchSendConfig(int batchSize, long delayBetweenBatches) {
        this.batchSize = batchSize;
        this.delayBetweenBatches = delayBetweenBatches;
    }

    // 链式配置方法
    public BatchSendConfig withBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public BatchSendConfig withDelayBetweenBatches(long delayBetweenBatches) {
        this.delayBetweenBatches = delayBetweenBatches;
        return this;
    }

    public BatchSendConfig withSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
        return this;
    }

    public BatchSendConfig withLogDetail(boolean logDetail) {
        this.logDetail = logDetail;
        return this;
    }

    public BatchSendConfig withMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    // Getters and Setters
    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getDelayBetweenBatches() {
        return delayBetweenBatches;
    }

    public void setDelayBetweenBatches(long delayBetweenBatches) {
        this.delayBetweenBatches = delayBetweenBatches;
    }

    public boolean isSkipOnError() {
        return skipOnError;
    }

    public void setSkipOnError(boolean skipOnError) {
        this.skipOnError = skipOnError;
    }

    public boolean isLogDetail() {
        return logDetail;
    }

    public void setLogDetail(boolean logDetail) {
        this.logDetail = logDetail;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
}