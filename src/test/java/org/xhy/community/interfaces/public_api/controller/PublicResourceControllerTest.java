package org.xhy.community.interfaces.public_api.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.xhy.community.application.permission.service.UserPermissionAppService;
import org.xhy.community.application.resource.service.ResourceAppService;
import org.xhy.community.application.session.service.TokenBlacklistAppService;
import org.xhy.community.infrastructure.config.JwtUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicResourceControllerTest {

    @Test
    void accessResourceAcceptsUrlTokenWhenCookieAndAuthorizationAreMissing() {
        ResourceAppService resourceAppService = mock(ResourceAppService.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        TokenBlacklistAppService tokenBlacklistAppService = mock(TokenBlacklistAppService.class);
        UserPermissionAppService userPermissionAppService = mock(UserPermissionAppService.class);
        PublicResourceController controller = new PublicResourceController(
                resourceAppService,
                jwtUtil,
                tokenBlacklistAppService,
                userPermissionAppService
        );

        String token = "url-token";
        String resourceId = "resource-id";
        String userId = "user-id";
        String signedUrl = "https://oss.xhyovo.cn/path/file.png?Expires=1";

        when(tokenBlacklistAppService.isBlacklisted(token)).thenReturn(false);
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        when(resourceAppService.getResourceAccessUrl(resourceId, userId)).thenReturn(signedUrl);

        ResponseEntity<Void> response = controller.accessResource(
                resourceId,
                null,
                null,
                token,
                new MockHttpServletRequest(),
                mock(HttpServletResponse.class)
        );

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertNotNull(response.getHeaders().getLocation());
        String location = response.getHeaders().getLocation().toString();
        assertTrue(location.startsWith(signedUrl));
        assertTrue(location.contains("&token=" + token));
    }
}
