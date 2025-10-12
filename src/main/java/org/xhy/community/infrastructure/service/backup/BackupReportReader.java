package org.xhy.community.infrastructure.service.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.infrastructure.config.BackupProperties;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BackupReportReader {
    private static final Logger log = LoggerFactory.getLogger(BackupReportReader.class);
    private final BackupProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BackupReportReader(BackupProperties properties) {
        this.properties = properties;
    }

    public List<BackupReport> listAll() {
        Path dir = Paths.get(properties.getReportsDir());
        if (!Files.isDirectory(dir)) {
            log.warn("[backup-report] 报表目录不存在或不可读: {}", dir);
            return Collections.emptyList();
        }
        try {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
                List<BackupReport> list = new ArrayList<>();
                for (Path p : stream) {
                    try {
                        BackupReport report = objectMapper.readValue(p.toFile(), BackupReport.class);
                        report.setReportFile(p.toString());
                        list.add(report);
                    } catch (IOException e) {
                        log.warn("[backup-report] 跳过无法解析的报表文件: file={}, error={}", p, e.getMessage());
                    }
                }
                // 按 finishedAt 倒序，如果为空则按文件名倒序
                return list.stream()
                        .sorted((a,b) -> Optional.ofNullable(b.getFinishedAt()).orElse("")
                                .compareTo(Optional.ofNullable(a.getFinishedAt()).orElse("")))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error("[backup-report] 读取报表目录失败: dir={}", dir, e);
            return Collections.emptyList();
        }
    }

    public Optional<BackupReport> latest() {
        return listAll().stream().findFirst();
    }

    public List<BackupReport> page(int pageNum, int pageSize, String status) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        List<BackupReport> all = listAll();
        if (status != null && !status.isBlank()) {
            all = all.stream().filter(r -> status.equalsIgnoreCase(r.getStatus())).collect(Collectors.toList());
        }
        int from = Math.min((pageNum - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        return all.subList(from, to);
    }

    /**
     * 与脚本生成的 JSON 字段对齐
     */
    public static class BackupReport {
        private String database;
        private String host;
        private String port;
        private String startedAt;   // ISO8601
        private String finishedAt;  // ISO8601
        private Long durationSeconds;
        private String mode;
        private String tool;
        private String status;      // SUCCESS/FAILED
        private String errorMessage;
        private Local local;
        private Remote remote;
        private transient String reportFile;

        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public String getPort() { return port; }
        public void setPort(String port) { this.port = port; }
        public String getStartedAt() { return startedAt; }
        public void setStartedAt(String startedAt) { this.startedAt = startedAt; }
        public String getFinishedAt() { return finishedAt; }
        public void setFinishedAt(String finishedAt) { this.finishedAt = finishedAt; }
        public Long getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
        public String getMode() { return mode; }
        public void setMode(String mode) { this.mode = mode; }
        public String getTool() { return tool; }
        public void setTool(String tool) { this.tool = tool; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public Local getLocal() { return local; }
        public void setLocal(Local local) { this.local = local; }
        public Remote getRemote() { return remote; }
        public void setRemote(Remote remote) { this.remote = remote; }
        public String getReportFile() { return reportFile; }
        public void setReportFile(String reportFile) { this.reportFile = reportFile; }

        public static class Local {
            private String file;
            private String globalsFile;
            private String checksumSha256;
            private Long sizeBytes;
            public String getFile() { return file; }
            public void setFile(String file) { this.file = file; }
            public String getGlobalsFile() { return globalsFile; }
            public void setGlobalsFile(String globalsFile) { this.globalsFile = globalsFile; }
            public String getChecksumSha256() { return checksumSha256; }
            public void setChecksumSha256(String checksumSha256) { this.checksumSha256 = checksumSha256; }
            public Long getSizeBytes() { return sizeBytes; }
            public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
        }

        public static class Remote {
            private List<String> urls;
            public List<String> getUrls() { return urls; }
            public void setUrls(List<String> urls) { this.urls = urls; }
        }
    }
}
