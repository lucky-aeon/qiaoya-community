package org.xhy.community.application.site.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhy.community.application.site.dto.CreatorAboutPageDTO;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.CreatorAboutPageConfig;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;
import org.xhy.community.infrastructure.integration.github.GithubRepositoryStarClient;

import java.util.ArrayList;
import java.util.List;

@Service
public class CreatorAboutPageAppService {

    private static final Logger log = LoggerFactory.getLogger(CreatorAboutPageAppService.class);

    private final SystemConfigDomainService systemConfigDomainService;
    private final GithubRepositoryStarClient githubRepositoryStarClient;

    public CreatorAboutPageAppService(SystemConfigDomainService systemConfigDomainService,
                                      GithubRepositoryStarClient githubRepositoryStarClient) {
        this.systemConfigDomainService = systemConfigDomainService;
        this.githubRepositoryStarClient = githubRepositoryStarClient;
    }

    public CreatorAboutPageDTO getPublicAboutPage() {
        CreatorAboutPageConfig config = systemConfigDomainService.getConfigData(SystemConfigType.CREATOR_ABOUT_PAGE, CreatorAboutPageConfig.class);
        if (config == null) {
            throw new BusinessException(SystemConfigErrorCode.CONFIG_NOT_FOUND, "关于我页面尚未配置");
        }

        CreatorAboutPageDTO dto = new CreatorAboutPageDTO();
        dto.setDisplayName(config.getDisplayName());
        dto.setIntroduction(config.getIntroduction());
        dto.setBilibiliUrl(config.getBilibiliUrl());
        dto.setGithubProfileUrl(config.getGithubProfileUrl());

        List<CreatorAboutPageDTO.ProjectDTO> projects = new ArrayList<>();
        for (CreatorAboutPageConfig.Project project : config.getProjects()) {
            CreatorAboutPageDTO.ProjectDTO projectDTO = new CreatorAboutPageDTO.ProjectDTO();
            projectDTO.setName(project.getName());
            projectDTO.setDescription(project.getDescription());
            projectDTO.setGithubUrl(project.getGithubUrl());
            try {
                Integer stars = githubRepositoryStarClient.getStarCount(project.getGithubUrl());
                projectDTO.setGithubStars(stars);
                log.info("【AboutPage】项目 Star 结果：projectName={}, githubUrl={}, stars={}",
                        project.getName(), project.getGithubUrl(), stars);
            } catch (RuntimeException e) {
                projectDTO.setGithubStars(null);
                log.warn("【AboutPage】项目 Star 获取异常：projectName={}, githubUrl={}",
                        project.getName(), project.getGithubUrl(), e);
            }
            projects.add(projectDTO);
        }
        dto.setProjects(projects);
        return dto;
    }
}
