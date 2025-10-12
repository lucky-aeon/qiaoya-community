package org.xhy.community.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "backup")
public class BackupProperties {
    /** 是否启用平台备份报表读取 */
    private boolean enabled = true;
    /** 备份报表目录（容器内可见路径） */
    private String reportsDir = "/data/db-backups/reports";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getReportsDir() {
        return reportsDir;
    }

    public void setReportsDir(String reportsDir) {
        this.reportsDir = reportsDir;
    }
}

