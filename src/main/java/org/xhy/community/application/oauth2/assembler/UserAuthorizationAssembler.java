package org.xhy.community.application.oauth2.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.oauth2.dto.UserAuthorizationDTO;
import org.xhy.community.domain.oauth2.entity.OAuth2AuthorizationEntity;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户授权信息 Assembler
 */
public class UserAuthorizationAssembler {

    /**
     * 将授权实体转换为 DTO
     * 需要关联客户端信息
     *
     * @param entity 授权实体
     * @param client 客户端实体
     * @return UserAuthorizationDTO
     */
    public static UserAuthorizationDTO toDTO(OAuth2AuthorizationEntity entity, OAuth2ClientEntity client) {
        if (entity == null) {
            return null;
        }

        UserAuthorizationDTO dto = new UserAuthorizationDTO();
        BeanUtils.copyProperties(entity, dto);

        // 设置客户端信息
        if (client != null) {
            dto.setClientName(client.getClientName());
            // OAuth2ClientEntity 暂无 logoUrl 和 description 字段
            dto.setClientLogoUrl(null);
            dto.setClientDescription(null);
        }

        // 设置授权范围
        dto.setScopes(entity.getAccessTokenScopes());

        // 设置 Access Token 是否有效
        dto.setAccessTokenValid(entity.isAccessTokenValid());

        return dto;
    }

    /**
     * 批量转换为 DTO 列表
     *
     * @param entities 授权实体列表
     * @param clientMap 客户端映射 (clientId -> OAuth2ClientEntity)
     * @return UserAuthorizationDTO 列表
     */
    public static List<UserAuthorizationDTO> toDTOList(
            List<OAuth2AuthorizationEntity> entities,
            Map<String, OAuth2ClientEntity> clientMap) {

        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream()
                .map(entity -> {
                    OAuth2ClientEntity client = clientMap.get(entity.getClientId());
                    return toDTO(entity, client);
                })
                .collect(Collectors.toList());
    }
}
