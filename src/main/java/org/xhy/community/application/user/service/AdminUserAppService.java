package org.xhy.community.application.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.user.assembler.AdminUserAssembler;
import org.xhy.community.application.user.dto.AdminUserDTO;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.query.UserQuery;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.user.valueobject.UserStatus;
import org.xhy.community.interfaces.user.request.AdminUserQueryRequest;

/**
 * 管理员用户应用服务
 * 处理管理员对用户的管理操作
 */
@Service
public class AdminUserAppService {
    
    private final UserDomainService userDomainService;
    
    public AdminUserAppService(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }
    
    /**
     * 分页查询用户列表
     * 支持邮箱、昵称、状态条件查询
     * 
     * @param request 查询请求参数
     * @return 用户分页列表
     */
    public IPage<AdminUserDTO> queryUsers(AdminUserQueryRequest request) {
        UserQuery query = AdminUserAssembler.fromQueryRequest(request);
        IPage<UserEntity> userPage = userDomainService.queryUsers(query);
        
        return AdminUserAssembler.toDTOPage(userPage);
    }
    
    /**
     * 切换用户状态
     * 自动在ACTIVE和INACTIVE之间切换
     * 
     * @param userId 用户ID
     * @return 更新后的用户信息
     */
    public AdminUserDTO toggleUserStatus(String userId) {
        UserEntity user = userDomainService.toggleUserStatus(userId);
        return AdminUserAssembler.toDTO(user);
    }
}