package org.xhy.community.application.codex.dto;

import java.util.List;

public class CodexConfigSetDTO {
    private List<CodexInstanceDTO> instances;

    public List<CodexInstanceDTO> getInstances() { return instances; }
    public void setInstances(List<CodexInstanceDTO> instances) { this.instances = instances; }
}
