package org.xhy.community.domain.skill.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.xhy.community.domain.common.entity.BaseEntity;

@TableName("skills")
public class SkillEntity extends BaseEntity {

    @TableField("user_id")
    private String userId;

    private String name;

    private String summary;

    private String description;

    @TableField("github_url")
    private String githubUrl;

    public SkillEntity() {
    }

    public SkillEntity(String userId, String name, String summary, String description, String githubUrl) {
        this.userId = userId;
        this.name = name;
        this.summary = summary;
        this.description = description;
        this.githubUrl = githubUrl;
    }

    public void updateContent(String name, String summary, String description, String githubUrl) {
        this.name = name;
        this.summary = summary;
        this.description = description;
        this.githubUrl = githubUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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
