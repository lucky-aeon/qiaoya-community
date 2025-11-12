package org.xhy.community.application.oauth2.assembler;

import org.xhy.community.application.oauth2.dto.OIDCUserInfoDTO;
import org.xhy.community.domain.user.entity.UserEntity;

import java.util.Set;

/**
 * OIDC UserInfo 转换器
 */
public class OIDCUserInfoAssembler {

    /**
     * 按 scope 组装 UserInfo
     */
    public static OIDCUserInfoDTO toDTO(UserEntity user, Set<String> scopes) {
        if (user == null) {
            return null;
        }
        OIDCUserInfoDTO dto = new OIDCUserInfoDTO();

        // openid：必须包含 sub
        dto.setSub(user.getId());

        // profile：name / preferred_username / picture
        if (scopes.contains("profile")) {
            String name = user.getName();
            dto.setName(name);
            // preferred_username：优先昵称，否则取邮箱前缀
            String preferred = name;
            if (preferred == null || preferred.isBlank()) {
                String email = user.getEmail();
                if (email != null && email.contains("@")) {
                    preferred = email.substring(0, email.indexOf('@'));
                }
            }
            dto.setPreferredUsername(preferred);
            dto.setPicture(user.getAvatar());
        }

        // email：email / email_verified
        if (scopes.contains("email")) {
            dto.setEmail(user.getEmail());
            // 当前系统未持久化邮箱验证状态，返回 null 则不会输出该字段
            // 若未来有验证状态，可在此填充 true/false
            dto.setEmailVerified(null);
        }

        return dto;
    }
}

