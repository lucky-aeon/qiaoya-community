package org.xhy.community.domain.config.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatorAboutPageConfig {

    private String displayName;
    private String introduction;
    private String bilibiliUrl;
    private String githubProfileUrl;
    private List<Project> projects = new ArrayList<>();

    public void normalize() {
        displayName = trim(displayName);
        introduction = trim(introduction);
        bilibiliUrl = trim(bilibiliUrl);
        githubProfileUrl = trim(githubProfileUrl);

        List<Project> normalizedProjects = new ArrayList<>();
        if (projects != null) {
            for (Project project : projects) {
                if (project == null) {
                    continue;
                }
                Project normalized = project.normalizedCopy();
                if (normalized.hasContent()) {
                    normalizedProjects.add(normalized);
                }
            }
        }
        projects = normalizedProjects;
    }

    public void validate() {
        validateLength(displayName, 1, 50, "名称");
        validateLength(introduction, 1, 2000, "个人介绍");
        validateHost(bilibiliUrl, "bilibili.com", "B站链接");
        validateHost(githubProfileUrl, "github.com", "GitHub主页链接");

        if (projects == null || projects.isEmpty()) {
            throw new IllegalArgumentException("项目列表不能为空");
        }
        if (projects.size() > 12) {
            throw new IllegalArgumentException("项目数量不能超过12个");
        }
        for (Project project : projects) {
            project.validate();
        }
    }

    private void validateLength(String value, int min, int max, String fieldName) {
        if (value == null || value.length() < min || value.length() > max) {
            throw new IllegalArgumentException(fieldName + "长度不合法");
        }
    }

    private void validateHost(String value, String expectedHost, String fieldName) {
        URI uri = parseUri(value, fieldName);
        String host = uri.getHost();
        if (host == null || !(host.equals(expectedHost) || host.endsWith("." + expectedHost))) {
            throw new IllegalArgumentException(fieldName + "域名不合法");
        }
    }

    private URI parseUri(String value, String fieldName) {
        try {
            URI uri = new URI(value);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException(fieldName + "不是合法URL");
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(fieldName + "不是合法URL");
        }
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

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

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {
        private String name;
        private String description;
        private String githubUrl;

        public Project normalizedCopy() {
            Project project = new Project();
            project.setName(trim(name));
            project.setDescription(trim(description));
            project.setGithubUrl(trim(githubUrl));
            return project;
        }

        public boolean hasContent() {
            return !isBlank(name) || !isBlank(description) || !isBlank(githubUrl);
        }

        public void validate() {
            validateLength(name, 1, 80, "项目名称");
            validateLength(description, 1, 200, "项目描述");
            validateGithubRepoUrl(githubUrl);
        }

        private void validateLength(String value, int min, int max, String fieldName) {
            if (value == null || value.length() < min || value.length() > max) {
                throw new IllegalArgumentException(fieldName + "长度不合法");
            }
        }

        private void validateGithubRepoUrl(String value) {
            RepoCoordinates.fromUrl(value);
        }

        private String trim(String value) {
            return value == null ? null : value.trim();
        }

        private boolean isBlank(String value) {
            return value == null || value.isBlank();
        }

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
    }

    public record RepoCoordinates(String owner, String repo) {
        public static RepoCoordinates fromUrl(String githubUrl) {
            if (githubUrl == null || githubUrl.isBlank()) {
                throw new IllegalArgumentException("项目GitHub链接不能为空");
            }
            try {
                URI uri = new URI(githubUrl.trim());
                String host = uri.getHost();
                if (host == null || !(host.equals("github.com") || host.endsWith(".github.com"))) {
                    throw new IllegalArgumentException("项目GitHub链接域名不合法");
                }
                String path = Objects.toString(uri.getPath(), "");
                String[] segments = path.split("/");
                List<String> parts = new ArrayList<>();
                for (String segment : segments) {
                    if (!segment.isBlank()) {
                        parts.add(segment);
                    }
                }
                if (parts.size() < 2) {
                    throw new IllegalArgumentException("项目GitHub链接无法解析仓库");
                }
                return new RepoCoordinates(parts.get(0), parts.get(1));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("项目GitHub链接不是合法URL");
            }
        }
    }
}
