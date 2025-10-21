package org.xhy.community.application.oauth2.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.oauth2.dto.OAuth2ClientDTO;
import org.xhy.community.domain.oauth2.entity.OAuth2ClientEntity;
import org.xhy.community.interfaces.oauth2.request.CreateOAuth2ClientRequest;
import org.xhy.community.interfaces.oauth2.request.UpdateOAuth2ClientRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OAuth2 客户端转换器
 * 使用静态方法进行 Entity 和 DTO 之间的转换
 */
public class OAuth2ClientAssembler {

    /**
     * Entity 转 DTO
     */
    public static OAuth2ClientDTO toDTO(OAuth2ClientEntity entity) {
        if (entity == null) {
            return null;
        }

        OAuth2ClientDTO dto = new OAuth2ClientDTO();
        BeanUtils.copyProperties(entity, dto);

        // 枚举类型转换为字符串
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().name());
        }

        return dto;
    }

    /**
     * Entity 列表转 DTO 列表
     */
    public static List<OAuth2ClientDTO> toDTOList(List<OAuth2ClientEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
            .map(OAuth2ClientAssembler::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * CreateRequest 转 Entity
     */
    public static OAuth2ClientEntity fromCreateRequest(CreateOAuth2ClientRequest request) {
        if (request == null) {
            return null;
        }

        OAuth2ClientEntity entity = new OAuth2ClientEntity();
        BeanUtils.copyProperties(request, entity);
        return entity;
    }

    /**
     * UpdateRequest 转 Entity
     */
    public static OAuth2ClientEntity fromUpdateRequest(UpdateOAuth2ClientRequest request, String id) {
        if (request == null) {
            return null;
        }

        OAuth2ClientEntity entity = new OAuth2ClientEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setId(id);
        return entity;
    }
}
