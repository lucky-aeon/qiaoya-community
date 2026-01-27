package org.xhy.community.domain.codex.valueobject;

import java.util.ArrayList;
import java.util.List;

/**
 * Codex 多实例配置集合，持久化在 SystemConfig(type=CODEX_CONFIGS) 的 JSON 中。
 * 说明：不再维护默认实例，每个实例都可用，由调用方选择。
 */
public class CodexConfigSet {
    private List<CodexInstance> instances; // 实例列表

    public CodexConfigSet() {
        this.instances = new ArrayList<>();
    }

    public CodexConfigSet(List<CodexInstance> instances) {
        this.instances = (instances != null) ? instances : new ArrayList<>();
    }

    public List<CodexInstance> getInstances() { return instances; }
    public void setInstances(List<CodexInstance> instances) { this.instances = instances; }
}
