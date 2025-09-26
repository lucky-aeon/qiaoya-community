package org.xhy.community.application.subscription.dto;

public class MenuOptionDTO {
    private String code;
    private String label;
    private String group;
    private String path;

    public MenuOptionDTO() {}

    public MenuOptionDTO(String code, String label, String group, String path) {
        this.code = code;
        this.label = label;
        this.group = group;
        this.path = path;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}

