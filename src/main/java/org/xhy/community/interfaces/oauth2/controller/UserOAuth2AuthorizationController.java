package org.xhy.community.interfaces.oauth2.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.oauth2.dto.UserAuthorizationDTO;
import org.xhy.community.application.oauth2.service.UserOAuth2AuthorizationAppService;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.oauth2.request.GetUserAuthorizationsRequest;

/**
 * 用户 OAuth2 授权管理控制器
 * 路由：/api/user/oauth2/authorizations
 *
 * 用户查看和管理自己已授权的第三方应用
 */
@RestController
@RequestMapping("/api/user/oauth2/authorizations")
@Validated
public class UserOAuth2AuthorizationController {

    private final UserOAuth2AuthorizationAppService userOAuth2AuthorizationAppService;

    public UserOAuth2AuthorizationController(UserOAuth2AuthorizationAppService userOAuth2AuthorizationAppService) {
        this.userOAuth2AuthorizationAppService = userOAuth2AuthorizationAppService;
    }

    /**
     * 获取用户的所有已授权应用列表（分页）
     *
     * @param request 分页查询请求参数
     * @return 已授权应用分页列表
     */
    @GetMapping
    public ApiResponse<IPage<UserAuthorizationDTO>> getUserAuthorizations(GetUserAuthorizationsRequest request) {
        String userId = UserContext.getCurrentUserId();
        IPage<UserAuthorizationDTO> authorizations =
            userOAuth2AuthorizationAppService.getUserAuthorizations(userId, request);
        return ApiResponse.success(authorizations);
    }

    /**
     * 撤销对某个第三方应用的授权
     *
     * @param clientId 客户端ID
     * @return 操作结果
     */
    @DeleteMapping("/{clientId}")
    public ApiResponse<Void> revokeAuthorization(
            @PathVariable @NotBlank(message = "客户端ID不能为空") String clientId) {
        String userId = UserContext.getCurrentUserId();
        userOAuth2AuthorizationAppService.revokeAuthorization(userId, clientId);
        return ApiResponse.success("解绑成功");
    }
}
