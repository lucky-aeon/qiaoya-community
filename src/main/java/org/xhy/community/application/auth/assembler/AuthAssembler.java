package org.xhy.community.application.auth.assembler;

import org.springframework.beans.BeanUtils;
import org.xhy.community.application.auth.dto.AdminSocialAccountDTO;
import org.xhy.community.application.auth.dto.UserSocialBindStatusDTO;
import org.xhy.community.application.user.assembler.UserAssembler;
import org.xhy.community.application.user.dto.UserDTO;
import org.xhy.community.domain.auth.entity.UserSocialAccountEntity;
import org.xhy.community.domain.auth.query.UserSocialAccountQuery;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.common.valueobject.AuthProvider;
import org.xhy.community.interfaces.oauth.request.AdminSocialAccountQueryRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthAssembler {

    public static UserDTO toUserDTO(UserEntity entity) {
        return UserAssembler.toDTO(entity);
    }

    public static UserSocialBindStatusDTO toBindStatusDTO(UserSocialAccountEntity entity) {
        UserSocialBindStatusDTO dto = new UserSocialBindStatusDTO();
        if (entity == null) { dto.setBound(false); return dto; }
        dto.setBound(true);
        dto.setLogin(entity.getLogin());
        dto.setAvatarUrl(entity.getAvatarUrl());
        dto.setProvider(entity.getProvider() != null ? entity.getProvider().name() : null);
        return dto;
    }

    public static AdminSocialAccountDTO toAdminDTO(UserSocialAccountEntity entity, String userEmail) {
        AdminSocialAccountDTO dto = new AdminSocialAccountDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setProvider(entity.getProvider() != null ? entity.getProvider().name() : null);
        dto.setUserEmail(userEmail);
        return dto;
    }

    public static List<AdminSocialAccountDTO> toAdminDTOList(List<UserSocialAccountEntity> list,
                                                             Map<String, String> userEmailMap) {
        return list.stream()
                .map(e -> toAdminDTO(e, userEmailMap.get(e.getUserId())))
                .collect(Collectors.toList());
    }

    public static UserSocialAccountQuery fromAdminQueryRequest(AdminSocialAccountQueryRequest request) {
        UserSocialAccountQuery q = new UserSocialAccountQuery();
        q.setPageNum(request.getPageNum());
        q.setPageSize(request.getPageSize());
        q.setUserId(request.getUserId());
        q.setLogin(request.getLogin());
        q.setStartTime(request.getStartTime());
        q.setEndTime(request.getEndTime());
        if (request.getProvider() != null && !request.getProvider().isBlank()) {
            try { q.setProvider(AuthProvider.valueOf(request.getProvider().toUpperCase())); } catch (Exception ignored) {}
        }
        return q;
    }
}
