package org.xhy.community.interfaces.skill.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateSkillRequest {

    @NotBlank(message = "技能名称不能为空")
    @Size(max = 200, message = "技能名称不能超过200个字符")
    private String name;

    @NotBlank(message = "技能摘要不能为空")
    @Size(max = 500, message = "技能摘要不能超过500个字符")
    private String summary;

    @NotBlank(message = "技能描述不能为空")
    private String description;

    @NotBlank(message = "GitHub 链接不能为空")
    @Size(max = 500, message = "GitHub 链接不能超过500个字符")
    private String githubUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }
}
