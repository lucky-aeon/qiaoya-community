package org.xhy.community.application.site.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xhy.community.application.site.dto.CreatorAboutPageDTO;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.CreatorAboutPageConfig;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;
import org.xhy.community.infrastructure.integration.github.GithubRepositoryStarClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatorAboutPageAppServiceTest {

    @Mock
    private SystemConfigDomainService systemConfigDomainService;

    @Mock
    private GithubRepositoryStarClient githubRepositoryStarClient;

    private CreatorAboutPageAppService creatorAboutPageAppService;

    @BeforeEach
    void setUp() {
        creatorAboutPageAppService = new CreatorAboutPageAppService(systemConfigDomainService, githubRepositoryStarClient);
    }

    @Test
    void shouldThrowWhenCreatorAboutPageConfigMissing() {
        when(systemConfigDomainService.getConfigData(SystemConfigType.CREATOR_ABOUT_PAGE, CreatorAboutPageConfig.class))
                .thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> creatorAboutPageAppService.getPublicAboutPage());

        assertEquals(SystemConfigErrorCode.CONFIG_NOT_FOUND.getCode(), exception.getCode());
    }

    @Test
    void shouldReturnNullStarsWhenGithubLookupFails() {
        when(systemConfigDomainService.getConfigData(SystemConfigType.CREATOR_ABOUT_PAGE, CreatorAboutPageConfig.class))
                .thenReturn(validConfig());
        when(githubRepositoryStarClient.getStarCount("https://github.com/xhyqaq/qiaoya-community"))
                .thenThrow(new RuntimeException("boom"));

        CreatorAboutPageDTO dto = creatorAboutPageAppService.getPublicAboutPage();

        assertEquals("Xhy", dto.getDisplayName());
        assertEquals(1, dto.getProjects().size());
        assertNull(dto.getProjects().get(0).getGithubStars());
    }

    @Test
    void shouldReturnStarCountWhenGithubLookupSucceeds() {
        when(systemConfigDomainService.getConfigData(SystemConfigType.CREATOR_ABOUT_PAGE, CreatorAboutPageConfig.class))
                .thenReturn(validConfig());
        when(githubRepositoryStarClient.getStarCount("https://github.com/xhyqaq/qiaoya-community"))
                .thenReturn(123);

        CreatorAboutPageDTO dto = creatorAboutPageAppService.getPublicAboutPage();

        assertEquals(123, dto.getProjects().get(0).getGithubStars());
    }

    private CreatorAboutPageConfig validConfig() {
        CreatorAboutPageConfig config = new CreatorAboutPageConfig();
        config.setDisplayName("Xhy");
        config.setIntroduction("技术创作者");
        config.setBilibiliUrl("https://space.bilibili.com/152686439");
        config.setGithubProfileUrl("https://github.com/xhyqaq");

        CreatorAboutPageConfig.Project project = new CreatorAboutPageConfig.Project();
        project.setName("qiaoya-community");
        project.setDescription("社区项目");
        project.setGithubUrl("https://github.com/xhyqaq/qiaoya-community");
        config.setProjects(List.of(project));
        return config;
    }
}
