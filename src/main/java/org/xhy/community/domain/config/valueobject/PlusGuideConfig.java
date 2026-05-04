package org.xhy.community.domain.config.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlusGuideConfig {

    /** 兼容旧配置：默认 CLI 安装命令，为空时导览中不显示该步骤 */
    private String installCommand;
    /** 多平台 CLI 安装命令，建议包含 macosLinux 和 windows */
    private Map<String, String> installCommands;

    public PlusGuideConfig() {
    }

    public PlusGuideConfig(String installCommand) {
        this.installCommand = installCommand;
    }

    public PlusGuideConfig(String installCommand, Map<String, String> installCommands) {
        this.installCommand = installCommand;
        this.installCommands = installCommands;
    }

    public String getInstallCommand() {
        return installCommand;
    }

    public void setInstallCommand(String installCommand) {
        this.installCommand = installCommand;
    }

    public Map<String, String> getInstallCommands() {
        return installCommands;
    }

    public void setInstallCommands(Map<String, String> installCommands) {
        this.installCommands = installCommands;
    }
}
