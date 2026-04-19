package org.xhy.community.application.site.dto;

import java.util.ArrayList;
import java.util.List;

public class CreatorAboutPageDTO {

    private String displayName;
    private String introduction;
    private String bilibiliUrl;
    private String githubProfileUrl;
    private List<ProjectDTO> projects = new ArrayList<>();

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getBilibiliUrl() {
        return bilibiliUrl;
    }

    public void setBilibiliUrl(String bilibiliUrl) {
        this.bilibiliUrl = bilibiliUrl;
    }

    public String getGithubProfileUrl() {
        return githubProfileUrl;
    }

    public void setGithubProfileUrl(String githubProfileUrl) {
        this.githubProfileUrl = githubProfileUrl;
    }

    public List<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }

    public static class ProjectDTO {
        private String name;
        private String description;
        private String githubUrl;
        private Integer githubStars;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public Integer getGithubStars() {
            return githubStars;
        }

        public void setGithubStars(Integer githubStars) {
            this.githubStars = githubStars;
        }
    }
}
