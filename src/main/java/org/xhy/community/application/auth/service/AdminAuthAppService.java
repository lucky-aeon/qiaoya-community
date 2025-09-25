package org.xhy.community.application.auth.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.xhy.community.application.auth.assembler.AuthAssembler;
import org.xhy.community.application.auth.dto.AdminSocialAccountDTO;
import org.xhy.community.domain.auth.entity.UserSocialAccountEntity;
import org.xhy.community.domain.auth.query.UserSocialAccountQuery;
import org.xhy.community.domain.auth.service.AuthDomainService;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.interfaces.oauth.request.AdminSocialAccountQueryRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminAuthAppService {

    private final AuthDomainService authDomainService;
    private final UserDomainService userDomainService;

    public AdminAuthAppService(AuthDomainService authDomainService,
                               UserDomainService userDomainService) {
        this.authDomainService = authDomainService;
        this.userDomainService = userDomainService;
    }

    public IPage<AdminSocialAccountDTO> pageSocialAccounts(AdminSocialAccountQueryRequest request) {
        UserSocialAccountQuery query = AuthAssembler.fromAdminQueryRequest(request);
        IPage<UserSocialAccountEntity> result = authDomainService.pageSocialAccounts(query);

        // 批量查询用户邮箱（通过UserDomainService）
        Set<String> uids = result.getRecords().stream().map(UserSocialAccountEntity::getUserId).collect(Collectors.toSet());
        List<UserEntity> users = userDomainService.getUsersByIds(uids);
        Map<String, String> emailMap = users.stream().collect(Collectors.toMap(UserEntity::getId, UserEntity::getEmail));

        List<AdminSocialAccountDTO> dtos = AuthAssembler.toAdminDTOList(result.getRecords(), emailMap);
        Page<AdminSocialAccountDTO> dtoPage = new Page<>(query.getPageNum(), query.getPageSize(), result.getTotal());
        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    public AdminSocialAccountDTO getById(String id) {
        UserSocialAccountEntity entity = authDomainService.getBindingById(id);
        if (entity == null) { return null; }
        UserEntity user = userDomainService.getUserById(entity.getUserId());
        return AuthAssembler.toAdminDTO(entity, user != null ? user.getEmail() : null);
    }

    public void adminUnbindById(String id) {
        authDomainService.unbindById(id);
    }
}
