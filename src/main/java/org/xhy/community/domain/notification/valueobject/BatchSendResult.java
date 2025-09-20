package org.xhy.community.domain.notification.valueobject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量发送结果
 */
public class BatchSendResult {

    private int totalCount;                    // 总数量
    private int successCount;                  // 成功数量
    private int failedCount;                   // 失败数量
    private int skippedCount;                  // 跳过数量
    private long totalTimeMs;                  // 总耗时（毫秒）
    private int batchCount;                    // 批次数量
    private LocalDateTime startTime;           // 开始时间
    private LocalDateTime endTime;             // 结束时间
    private List<FailedItem> failedItems;      // 失败项详情

    public BatchSendResult() {
        this.failedItems = new ArrayList<>();
        this.startTime = LocalDateTime.now();
    }

    /**
     * 完成批量发送
     */
    public void complete() {
        this.endTime = LocalDateTime.now();
        this.totalTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
    }

    /**
     * 增加成功数量
     */
    public void incrementSuccess() {
        this.successCount++;
    }

    /**
     * 增加失败数量
     */
    public void incrementFailed() {
        this.failedCount++;
    }

    /**
     * 增加跳过数量
     */
    public void incrementSkipped() {
        this.skippedCount++;
    }

    /**
     * 添加失败项
     */
    public void addFailedItem(String userId, String email, String reason) {
        this.failedItems.add(new FailedItem(userId, email, reason));
        incrementFailed();
    }

    /**
     * 获取成功率（百分比）
     */
    public double getSuccessRate() {
        if (totalCount == 0) return 0.0;
        return (double) successCount / totalCount * 100;
    }

    // Getters and Setters
    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public long getTotalTimeMs() {
        return totalTimeMs;
    }

    public void setTotalTimeMs(long totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<FailedItem> getFailedItems() {
        return failedItems;
    }

    public void setFailedItems(List<FailedItem> failedItems) {
        this.failedItems = failedItems;
    }

    /**
     * 失败项详情
     */
    public static class FailedItem {
        private final String userId;
        private final String email;
        private final String reason;
        private final LocalDateTime failTime;

        public FailedItem(String userId, String email, String reason) {
            this.userId = userId;
            this.email = email;
            this.reason = reason;
            this.failTime = LocalDateTime.now();
        }

        public String getUserId() {
            return userId;
        }

        public String getEmail() {
            return email;
        }

        public String getReason() {
            return reason;
        }

        public LocalDateTime getFailTime() {
            return failTime;
        }
    }
}