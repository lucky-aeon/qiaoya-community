package org.xhy.community.application.codex.dto;

/**
 * 前台用于展示的 Codex 实例公共信息（含实例标识）
 */
public class CodexPublicInstanceDTO extends CodexPublicInfoDTO {
    private String id;
    private String name;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

