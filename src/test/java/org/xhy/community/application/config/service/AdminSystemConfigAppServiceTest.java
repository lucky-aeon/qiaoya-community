package org.xhy.community.application.config.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xhy.community.application.config.dto.SystemConfigDTO;
import org.xhy.community.domain.config.entity.SystemConfigEntity;
import org.xhy.community.domain.config.service.SystemConfigDomainService;
import org.xhy.community.domain.config.valueobject.SystemConfigType;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.SystemConfigErrorCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminSystemConfigAppServiceTest {

    @Mock
    private SystemConfigDomainService systemConfigDomainService;

    @Mock
    private SubscriptionPlanDomainService subscriptionPlanDomainService;

    private AdminSystemConfigAppService adminSystemConfigAppService;

    @BeforeEach
    void setUp() {
        adminSystemConfigAppService = new AdminSystemConfigAppService(
                systemConfigDomainService,
                subscriptionPlanDomainService,
                new ObjectMapper()
        );
    }

    @Test
    void shouldRejectEmptyProjectsWhenUpdateCreatorAboutPage() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("displayName", "Xhy");
        payload.put("introduction", "技术创作者");
        payload.put("bilibiliUrl", "https://space.bilibili.com/152686439");
        payload.put("githubProfileUrl", "https://github.com/xhyqaq");
        payload.put("projects", List.of());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                adminSystemConfigAppService.updateConfigByType(SystemConfigType.CREATOR_ABOUT_PAGE, payload)
        );

        assertEquals(SystemConfigErrorCode.INVALID_CONFIG_DATA.getCode(), exception.getCode());
        verify(systemConfigDomainService, never()).updateConfigData(any(), any());
    }

    @Test
    void shouldPersistNormalizedCreatorAboutPageConfig() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("displayName", " Xhy ");
        payload.put("introduction", " 持续做技术内容和项目输出 ");
        payload.put("bilibiliUrl", "https://space.bilibili.com/152686439");
        payload.put("githubProfileUrl", "https://github.com/xhyqaq");
        payload.put("projects", List.of(Map.of(
                "name", " qiaoya-community ",
                "description", " 社区项目 ",
                "githubUrl", "https://github.com/xhyqaq/qiaoya-community"
        )));

        adminSystemConfigAppService.updateConfigByType(SystemConfigType.CREATOR_ABOUT_PAGE, payload);

        verify(systemConfigDomainService).updateConfigData(eq(SystemConfigType.CREATOR_ABOUT_PAGE), any());
    }

    @Test
    void shouldReturnNullDtoWhenCreatorAboutPageConfigMissing() {
        SystemConfigDTO dto = adminSystemConfigAppService.getConfigByType(SystemConfigType.CREATOR_ABOUT_PAGE);
        assertEquals(null, dto.getData());
    }
}
