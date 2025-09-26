package org.xhy.community.application.subscription.dto;

public class PermissionOptionDTO {
    private String code;
    private String label;
    private String group;

    public PermissionOptionDTO() {}

    public PermissionOptionDTO(String code, String label, String group) {
        this.code = code;
        this.label = label;
        this.group = group;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }
}

