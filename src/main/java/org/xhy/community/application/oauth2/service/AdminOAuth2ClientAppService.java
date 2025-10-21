package org.xhy.community.application.oauth2.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.oauth2.assembler.OAuth2ClientAssembler;
import org.xhy.community.application.oauth2.dto.OAuth2ClientDTO;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.domain.oauth2.service.OAuth2ClientDomainService;
import org.xhy.community.domain.oauth2.valueobject.OAuth2ClientStatus;
import org.xhy.community.interfaces.oauth2.request.CreateOAuth2ClientRequest;
import org.xhy.community.interfaces.oauth2.request.UpdateOAuth2ClientRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 客户端管理后台应用服务
 * 负责业务流程编排，调用 Domain 服务完成操作
 */
@Service
public class AdminOAuth2ClientAppService {

    private final OAuth2ClientDomainService oauth2ClientDomainService;

    public AdminOAuth2ClientAppService(OAuth2ClientDomainService oauth2ClientDomainService) {
        this.oauth2ClientDomainService = oauth2ClientDomainService;
    }

    /**
     * 创建OAuth2客户端
     * @param request 创建请求
     * @param createdBy 创建人用户ID
     * @return 包含 client 和 clientSecret 的 Map（clientSecret 仅此一次返回）
     */
    @Transactional
    public Map<String, Object> createClient(CreateOAuth2ClientRequest request, String createdBy) {
        // 转换 Request 为 Entity
        OAuth2ClientEntity entity = OAuth2ClientAssembler.fromCreateRequest(request);
        entity.setCreatedBy(createdBy);

        // 生成客户端密钥
        String plainSecret = oauth2ClientDomainService.generateClientSecret();
        entity.setClientSecretEnc(plainSecret);

        // 调用 Domain 服务创建客户端（会加密密钥）
        OAuth2ClientEntity createdClient = oauth2ClientDomainService.createClient(entity);

        // 转换为 DTO
        OAuth2ClientDTO dto = OAuth2ClientAssembler.toDTO(createdClient);

        // 返回结果（包含明文密钥，仅此一次）
        Map<String, Object> result = new HashMap<>();
        result.put("client", dto);
        result.put("clientSecret", plainSecret);
        return result;
    }

    /**
     * 更新OAuth2客户端
     */
    @Transactional
    public OAuth2ClientDTO updateClient(String clientId, UpdateOAuth2ClientRequest request) {
        OAuth2ClientEntity entity = OAuth2ClientAssembler.fromUpdateRequest(request, clientId);
        OAuth2ClientEntity updatedClient = oauth2ClientDomainService.updateClient(entity);
        return OAuth2ClientAssembler.toDTO(updatedClient);
    }

    /**
     * 重新生成客户端密钥
     * @return 新的客户端密钥（明文，仅此一次返回）
     */
    @Transactional
    public Map<String, String> regenerateClientSecret(String clientId) {
        String newSecret = oauth2ClientDomainService.regenerateClientSecret(clientId);

        Map<String, String> result = new HashMap<>();
        result.put("clientId", clientId);
        result.put("clientSecret", newSecret);
        return result;
    }

    /**
     * 根据ID查询客户端
     */
    public OAuth2ClientDTO getClientById(String id) {
        OAuth2ClientEntity client = oauth2ClientDomainService.getClientById(id);
        return OAuth2ClientAssembler.toDTO(client);
    }

    /**
     * 分页查询客户端列表
     */
    public IPage<OAuth2ClientDTO> pageClients(Integer pageNum, Integer pageSize,
                                               String clientName, String status) {
        // 字符串转枚举
        OAuth2ClientStatus statusEnum = status != null ? OAuth2ClientStatus.valueOf(status) : null;

        // 调用 Domain 服务查询
        IPage<OAuth2ClientEntity> page = oauth2ClientDomainService.pageClients(
            pageNum, pageSize, clientName, statusEnum
        );

        // 转换为 DTO
        return page.convert(OAuth2ClientAssembler::toDTO);
    }

    /**
     * 删除客户端
     */
    @Transactional
    public void deleteClient(String id) {
        oauth2ClientDomainService.deleteClient(id);
    }

    /**
     * 激活客户端
     */
    @Transactional
    public void activateClient(String id) {
        OAuth2ClientEntity client = oauth2ClientDomainService.getClientById(id);
        client.activate();
        oauth2ClientDomainService.updateClient(client);
    }

    /**
     * 暂停客户端
     */
    @Transactional
    public void suspendClient(String id) {
        OAuth2ClientEntity client = oauth2ClientDomainService.getClientById(id);
        client.suspend();
        oauth2ClientDomainService.updateClient(client);
    }

    /**
     * 撤销客户端
     */
    @Transactional
    public void revokeClient(String id) {
        OAuth2ClientEntity client = oauth2ClientDomainService.getClientById(id);
        client.revoke();
        oauth2ClientDomainService.updateClient(client);
    }
}
