package org.xhy.community.application.oauth2.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.oauth2.assembler.UserAuthorizationAssembler;
import org.xhy.community.application.oauth2.dto.UserAuthorizationDTO;
import org.xhy.community.domain.oauth2.entity.OAuth2AuthorizationEntity;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.domain.oauth2.service.OAuth2AuthorizationDomainService;
import org.xhy.community.domain.oauth2.service.OAuth2ClientDomainService;
import org.xhy.community.interfaces.oauth2.request.GetUserAuthorizationsRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户 OAuth2 授权应用服务
 * 处理用户查看和管理已授权应用
 */
@Service
public class UserOAuth2AuthorizationAppService {

    private final OAuth2AuthorizationDomainService authorizationDomainService;
    private final OAuth2ClientDomainService clientDomainService;

    public UserOAuth2AuthorizationAppService(
            OAuth2AuthorizationDomainService authorizationDomainService,
            OAuth2ClientDomainService clientDomainService) {
        this.authorizationDomainService = authorizationDomainService;
        this.clientDomainService = clientDomainService;
    }

    /**
     * 获取用户的所有已授权应用列表（分页）
     *
     * @param userId 用户ID
     * @param request 分页查询请求参数
     * @return 已授权应用分页列表
     */
    public IPage<UserAuthorizationDTO> getUserAuthorizations(String userId, GetUserAuthorizationsRequest request) {
        // 调用领域服务进行分页查询
        IPage<OAuth2AuthorizationEntity> entityPage =
            authorizationDomainService.getUserAuthorizations(userId, request.getPageNum(), request.getPageSize());

        List<OAuth2AuthorizationEntity> authorizations = entityPage.getRecords();
        if (authorizations.isEmpty()) {
            Page<UserAuthorizationDTO> emptyPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
            emptyPage.setRecords(List.of());
            return emptyPage;
        }

        // 批量查询客户端信息
        Set<String> clientIds = authorizations.stream()
            .map(OAuth2AuthorizationEntity::getClientId)
            .collect(Collectors.toSet());

        Map<String, OAuth2ClientEntity> clientMap = clientDomainService.getClientMapByClientIds(clientIds);

        // 转换为 DTO
        List<UserAuthorizationDTO> dtoList = UserAuthorizationAssembler.toDTOList(authorizations, clientMap);

        // 构建分页结果
        Page<UserAuthorizationDTO> dtoPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    /**
     * 撤销用户对某个应用的授权
     *
     * @param userId 用户ID
     * @param clientId 客户端ID
     */
    public void revokeAuthorization(String userId, String clientId) {
        authorizationDomainService.revokeAuthorization(userId, clientId);
    }
}
