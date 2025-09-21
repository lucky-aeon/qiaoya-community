package org.xhy.community.application.session.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.application.session.assembler.DeviceSessionAssembler;
import org.xhy.community.application.session.dto.DeviceSessionQuery;
import org.xhy.community.application.session.dto.UserSessionSummaryDTO;
import org.xhy.community.domain.session.service.DeviceSessionDomainService;
import org.xhy.community.domain.session.valueobject.ActiveIpInfo;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.query.UserQuery;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.UserErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员设备会话应用服务
 */
@Service
public class AdminDeviceSessionAppService {

    private final UserDomainService userDomainService;
    private final DeviceSessionDomainService deviceSessionDomainService;

    public AdminDeviceSessionAppService(UserDomainService userDomainService,
                                      DeviceSessionDomainService deviceSessionDomainService) {
        this.userDomainService = userDomainService;
        this.deviceSessionDomainService = deviceSessionDomainService;
    }

    /**
     * 分页查询用户设备会话信息
     */
    public IPage<UserSessionSummaryDTO> queryUserSessions(DeviceSessionQuery query) {
        // 构建用户查询条件
        UserQuery userQuery = new UserQuery();
        userQuery.setPageNum(query.getPageNum());
        userQuery.setPageSize(query.getPageSize());
        if (StringUtils.hasText(query.getUsername())) {
            userQuery.setName(query.getUsername());
        }

        // 分页查询用户
        IPage<UserEntity> userPage = userDomainService.queryUsers(userQuery);

        // 组装用户会话汇总信息
        List<UserSessionSummaryDTO> sessionSummaries = new ArrayList<>();
        for (UserEntity user : userPage.getRecords()) {
            // 如果指定了userId过滤条件，跳过不匹配的用户
            if (StringUtils.hasText(query.getUserId()) && !query.getUserId().equals(user.getId())) {
                continue;
            }

            // 获取用户活跃IP信息
            List<ActiveIpInfo> activeIps = deviceSessionDomainService.getActiveIpsWithLastSeen(user.getId());

            // 如果指定了IP过滤条件，过滤活跃IP
            if (StringUtils.hasText(query.getIp())) {
                activeIps = activeIps.stream()
                        .filter(ip -> ip.getIp().contains(query.getIp()))
                        .collect(Collectors.toList());

                // 如果过滤后没有匹配的IP，跳过该用户
                if (activeIps.isEmpty()) {
                    continue;
                }
            }

            // 检查用户是否被封禁
            boolean isBanned = deviceSessionDomainService.isUserBanned(user.getId());

            // 组装DTO
            UserSessionSummaryDTO summary = DeviceSessionAssembler.toUserSessionSummaryDTO(user, activeIps, isBanned);
            sessionSummaries.add(summary);
        }

        // 构建分页结果
        Page<UserSessionSummaryDTO> resultPage = new Page<>(query.getPageNum(), query.getPageSize());
        resultPage.setRecords(sessionSummaries);
        resultPage.setTotal(userPage.getTotal());
        return resultPage;
    }

    /**
     * 强制下线指定用户的指定IP
     */
    public void forceRemoveUserSession(String userId, String ip) {
        // 验证用户是否存在
        UserEntity user = userDomainService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 验证IP是否在用户的活跃列表中
        List<ActiveIpInfo> activeIps = deviceSessionDomainService.getActiveIpsWithLastSeen(userId);
        boolean ipExists = activeIps.stream()
                .anyMatch(activeIp -> ip.equals(activeIp.getIp()));

        if (!ipExists) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND, "该IP不在用户的活跃设备列表中");
        }

        deviceSessionDomainService.forceRemoveActiveIp(userId, ip);
    }

    /**
     * 强制下线指定用户的所有设备
     */
    public void forceRemoveAllUserSessions(String userId) {
        // 验证用户是否存在
        UserEntity user = userDomainService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        deviceSessionDomainService.clearAllActiveIps(userId);
    }
}