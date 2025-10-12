package org.xhy.community.application.ops.dto;

import java.util.List;

public class BackupJobDTO {
    private String database;
    private String startedAt;
    private String finishedAt;
    private Long durationSeconds;
    private String mode;
    private String status;
    private Long sizeBytes;
    private String localFile;
    private List<String> remoteUrls;

    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }
    public String getStartedAt() { return startedAt; }
    public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
    public String getFinishedAt() { return finishedAt; }
    public void setFinishedAt(String finishedAt) { this.finishedAt = finishedAt; }
    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getLocalFile() { return localFile; }
    public void setLocalFile(String localFile) { this.localFile = localFile; }
    public List<String> getRemoteUrls() { return remoteUrls; }
    public void setRemoteUrls(List<String> remoteUrls) { this.remoteUrls = remoteUrls; }
}

