package org.xhy.community.interfaces.oauth2.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.xhy.community.application.oauth2.dto.OAuth2ClientDTO;
import org.xhy.community.application.oauth2.service.AdminOAuth2ClientAppService;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.interfaces.oauth2.request.CreateOAuth2ClientRequest;
import org.xhy.community.interfaces.oauth2.request.OAuth2ClientQueryRequest;
import org.xhy.community.interfaces.oauth2.request.UpdateOAuth2ClientRequest;

import java.util.Map;

/**
 * 管理员OAuth2客户端管理控制器
 * 提供OAuth2客户端的增删改查等管理功能，需要管理员权限
 * @module OAuth2客户端管理
 */
@RestController
@RequestMapping("/api/admin/oauth2/clients")
public class AdminOAuth2ClientController {

    private final AdminOAuth2ClientAppService adminOAuth2ClientAppService;

    public AdminOAuth2ClientController(AdminOAuth2ClientAppService adminOAuth2ClientAppService) {
        this.adminOAuth2ClientAppService = adminOAuth2ClientAppService;
    }

    /**
     * 创建OAuth2客户端
     * 管理员创建新的OAuth2客户端应用，返回客户端信息和密钥（密钥仅此一次返回）
     * @param request 创建OAuth2客户端请求参数
     * @return 包含客户端信息和密钥的响应（客户端密钥仅此一次返回，请妥善保管）
     */
    @PostMapping
    @ActivityLog(ActivityType.ADMIN_OAUTH2_CLIENT_CREATE)
    public ApiResponse<Map<String, Object>> createClient(@Valid @RequestBody CreateOAuth2ClientRequest request) {
        String currentUserId = UserContext.getCurrentUserId();
        Map<String, Object> result = adminOAuth2ClientAppService.createClient(request, currentUserId);
        return ApiResponse.success("创建成功，客户端密钥仅此一次返回，请妥善保管", result);
    }

    /**
     * 更新OAuth2客户端信息
     * 管理员更新OAuth2客户端的配置信息
     * @param clientId 客户端主键ID
     * @param request 更新OAuth2客户端请求参数
     * @return 更新后的客户端信息
     */
    @PutMapping("/{clientId}")
    @ActivityLog(ActivityType.ADMIN_OAUTH2_CLIENT_UPDATE)
    public ApiResponse<OAuth2ClientDTO> updateClient(@PathVariable String clientId,
                                                     @Valid @RequestBody UpdateOAuth2ClientRequest request) {
        OAuth2ClientDTO client = adminOAuth2ClientAppService.updateClient(clientId, request);
        return ApiResponse.success("更新成功", client);
    }

    /**
     * 重新生成客户端密钥
     * 管理员重新生成客户端密钥，旧密钥将立即失效
     * @param clientId 客户端主键ID
     * @return 包含新密钥的响应（新密钥仅此一次返回，请妥善保管）
     */
    @PostMapping("/{clientId}/regenerate-secret")
    @ActivityLog(ActivityType.ADMIN_OAUTH2_CLIENT_REGENERATE_SECRET)
    public ApiResponse<Map<String, String>> regenerateClientSecret(@PathVariable String clientId) {
        Map<String, String> result = adminOAuth2ClientAppService.regenerateClientSecret(clientId);
        return ApiResponse.success("密钥重新生成成功，旧密钥已失效，新密钥仅此一次返回，请妥善保管", result);
    }

    /**
     * 获取OAuth2客户端详情
     * 查看OAuth2客户端的详细信息
     * @param clientId 客户端主键ID
     * @return 客户端详情
     */
    @GetMapping("/{clientId}")
    public ApiResponse<OAuth2ClientDTO> getClient(@PathVariable String clientId) {
        OAuth2ClientDTO client = adminOAuth2ClientAppService.getClientById(clientId);
        return ApiResponse.success(client);
    }

    /**
     * 分页查询OAuth2客户端列表
     * 支持按客户端名称、状态等条件分页查询
     * @param request 查询请求参数（包含分页参数和筛选条件）
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<IPage<OAuth2ClientDTO>> getClients(OAuth2ClientQueryRequest request) {
        IPage<OAuth2ClientDTO> clients = adminOAuth2ClientAppService.pageClients(
            request.getPageNum(),
            request.getPageSize(),
            request.getClientName(),
            request.getStatus()
        );
        return ApiResponse.success(clients);
    }

    /**
     * 删除OAuth2客户端
     * 管理员删除OAuth2客户端（软删除）
     * @param clientId 客户端主键ID
     * @return 空响应
     */
    @DeleteMapping("/{clientId}")
    @ActivityLog(ActivityType.ADMIN_OAUTH2_CLIENT_DELETE)
    public ApiResponse<Void> deleteClient(@PathVariable String clientId) {
        adminOAuth2ClientAppService.deleteClient(clientId);
        return ApiResponse.success("删除成功");
    }

    /**
     * 激活OAuth2客户端
     * 将客户端状态设置为激活
     * @param clientId 客户端主键ID
     * @return 空响应
     */
    @PostMapping("/{clientId}/activate")
    @ActivityLog(ActivityType.ADMIN_OAUTH2_CLIENT_ACTIVATE)
    public ApiResponse<Void> activateClient(@PathVariable String clientId) {
        adminOAuth2ClientAppService.activateClient(clientId);
        return ApiResponse.success("客户端已激活");
    }

    /**
     * 暂停OAuth2客户端
     * 将客户端状态设置为暂停
     * @param clientId 客户端主键ID
     * @return 空响应
     */
    @PostMapping("/{clientId}/suspend")
    @ActivityLog(ActivityType.ADMIN_OAUTH2_CLIENT_SUSPEND)
    public ApiResponse<Void> suspendClient(@PathVariable String clientId) {
        adminOAuth2ClientAppService.suspendClient(clientId);
        return ApiResponse.success("客户端已暂停");
    }

    /**
     * 撤销OAuth2客户端
     * 将客户端状态设置为撤销（永久禁用）
     * @param clientId 客户端主键ID
     * @return 空响应
     */
    @PostMapping("/{clientId}/revoke")
    @ActivityLog(ActivityType.ADMIN_OAUTH2_CLIENT_REVOKE)
    public ApiResponse<Void> revokeClient(@PathVariable String clientId) {
        adminOAuth2ClientAppService.revokeClient(clientId);
        return ApiResponse.success("客户端已撤销");
    }
}
