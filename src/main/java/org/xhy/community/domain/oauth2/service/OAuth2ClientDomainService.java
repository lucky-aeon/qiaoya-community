package org.xhy.community.domain.oauth2.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.domain.oauth2.repository.OAuth2ClientRepository;
import org.xhy.community.domain.oauth2.valueobject.OAuth2ClientStatus;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.OAuth2ErrorCode;

import java.util.UUID;

/**
 * OAuth2 客户端领域服务
 * 负责客户端的核心业务逻辑
 */
@Service
public class OAuth2ClientDomainService {

    private final OAuth2ClientRepository oauth2ClientRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuth2ClientDomainService(OAuth2ClientRepository oauth2ClientRepository,
                                     PasswordEncoder passwordEncoder) {
        this.oauth2ClientRepository = oauth2ClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 创建OAuth2客户端
     * 注意：调用此方法前，需要在 client.clientSecretEnc 中设置明文密钥
     */
    public OAuth2ClientEntity createClient(OAuth2ClientEntity client) {
        // 业务规则：clientId 必须唯一
        validateClientIdUnique(client.getClientId(), null);

        // 验证密钥不为空
        String plainSecret = client.getClientSecretEnc();
        if (plainSecret == null || plainSecret.isEmpty()) {
            throw new IllegalArgumentException("客户端密钥不能为空");
        }

        // 加密存储客户端密钥
        String encryptedSecret = passwordEncoder.encode(plainSecret);
        client.setClientSecretEnc(encryptedSecret);

        oauth2ClientRepository.insert(client);
        return client;
    }

    /**
     * 更新OAuth2客户端
     */
    public OAuth2ClientEntity updateClient(OAuth2ClientEntity client) {
        // 业务规则：clientId 必须唯一
        validateClientIdUnique(client.getClientId(), client.getId());

        // 如果密钥被更新，需要重新加密
        OAuth2ClientEntity existing = oauth2ClientRepository.selectById(client.getId());
        if (existing == null) {
            throw new BusinessException(OAuth2ErrorCode.CLIENT_NOT_FOUND);
        }

        // 如果密钥被修改，重新加密
        if (!client.getClientSecretEnc().equals(existing.getClientSecretEnc())) {
            String encryptedSecret = passwordEncoder.encode(client.getClientSecretEnc());
            client.setClientSecretEnc(encryptedSecret);
        }

        oauth2ClientRepository.updateById(client);
        return client;
    }

    /**
     * 重新生成客户端密钥
     */
    public String regenerateClientSecret(String clientId) {
        OAuth2ClientEntity client = getClientByClientId(clientId);

        // 生成新的密钥
        String newSecret = generateClientSecret();
        String encryptedSecret = passwordEncoder.encode(newSecret);

        client.setClientSecretEnc(encryptedSecret);
        oauth2ClientRepository.updateById(client);

        // 返回明文密钥（仅此一次机会获取）
        return newSecret;
    }

    /**
     * 根据客户端ID查询客户端
     */
    public OAuth2ClientEntity getClientByClientId(String clientId) {
        LambdaQueryWrapper<OAuth2ClientEntity> queryWrapper = new LambdaQueryWrapper<OAuth2ClientEntity>()
            .eq(OAuth2ClientEntity::getClientId, clientId);

        OAuth2ClientEntity client = oauth2ClientRepository.selectOne(queryWrapper);
        if (client == null) {
            throw new BusinessException(OAuth2ErrorCode.CLIENT_NOT_FOUND);
        }
        return client;
    }

    /**
     * 根据ID查询客户端
     */
    public OAuth2ClientEntity getClientById(String id) {
        OAuth2ClientEntity client = oauth2ClientRepository.selectById(id);
        if (client == null) {
            throw new BusinessException(OAuth2ErrorCode.CLIENT_NOT_FOUND);
        }
        return client;
    }

    /**
     * 分页查询客户端列表
     */
    public IPage<OAuth2ClientEntity> pageClients(Integer pageNum, Integer pageSize,
                                                  String clientName, OAuth2ClientStatus status) {
        Page<OAuth2ClientEntity> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<OAuth2ClientEntity> queryWrapper = new LambdaQueryWrapper<OAuth2ClientEntity>()
            .like(clientName != null, OAuth2ClientEntity::getClientName, clientName)
            .eq(status != null, OAuth2ClientEntity::getStatus, status)
            .orderByDesc(OAuth2ClientEntity::getCreateTime);

        return oauth2ClientRepository.selectPage(page, queryWrapper);
    }

    /**
     * 验证客户端密钥
     */
    public boolean validateClientSecret(String clientId, String rawSecret) {
        OAuth2ClientEntity client = getClientByClientId(clientId);
        return passwordEncoder.matches(rawSecret, client.getClientSecretEnc());
    }

    /**
     * 删除客户端（软删除）
     */
    public void deleteClient(String id) {
        oauth2ClientRepository.deleteById(id);
    }

    // 私有方法

    /**
     * 验证clientId唯一性
     */
    private void validateClientIdUnique(String clientId, String excludeId) {
        LambdaQueryWrapper<OAuth2ClientEntity> queryWrapper = new LambdaQueryWrapper<OAuth2ClientEntity>()
            .eq(OAuth2ClientEntity::getClientId, clientId)
            .ne(excludeId != null, OAuth2ClientEntity::getId, excludeId);

        if (oauth2ClientRepository.exists(queryWrapper)) {
            throw new BusinessException(OAuth2ErrorCode.CLIENT_ID_ALREADY_EXISTS);
        }
    }

    /**
     * 生成客户端密钥（64位随机字符串）
     */
    public String generateClientSecret() {
        return UUID.randomUUID().toString().replace("-", "") +
               UUID.randomUUID().toString().replace("-", "");
    }
}
