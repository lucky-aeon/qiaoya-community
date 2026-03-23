package org.xhy.community.interfaces.skill.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateSkillRequest {

    private static final String GITHUB_URL_PATTERN = "^https://(www\\.)?github\\.com/.+$";

    @NotBlank(message = "Skill 名称不能为空")
    @Size(max = 100, message = "Skill 名称不能超过100个字符")
    private String name;

    @NotBlank(message = "Skill 简介不能为空")
    @Size(max = 255, message = "Skill 简介不能超过255个字符")
    private String summary;

    @NotBlank(message = "Skill 描述不能为空")
    @Size(max = 4000, message = "Skill 描述不能超过4000个字符")
    private String description;

    @NotBlank(message = "GitHub 链接不能为空")
    @Size(max = 500, message = "GitHub 链接不能超过500个字符")
    @Pattern(regexp = GITHUB_URL_PATTERN, message = "GitHub 链接格式不正确")
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
