package org.xhy.community.domain.config.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlusGuideConfig {

    /** CLI 安装命令，为空时导览中不显示该步骤 */
    private String installCommand;

    public PlusGuideConfig() {
    }

    public PlusGuideConfig(String installCommand) {
        this.installCommand = installCommand;
    }

    public String getInstallCommand() {
        return installCommand;
    }

    public void setInstallCommand(String installCommand) {
        this.installCommand = installCommand;
    }
}
