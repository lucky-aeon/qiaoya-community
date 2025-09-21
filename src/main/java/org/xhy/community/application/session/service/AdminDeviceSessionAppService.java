package org.xhy.community.application.session.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.xhy.community.application.session.assembler.DeviceSessionAssembler;
import org.xhy.community.application.session.dto.BlacklistedUserDTO;
import org.xhy.community.application.session.dto.DeviceSessionQuery;
import org.xhy.community.application.session.dto.TokenBlacklistStatsDTO;
import org.xhy.community.application.session.dto.UserSessionSummaryDTO;
import org.xhy.community.domain.session.service.DeviceSessionDomainService;
import org.xhy.community.domain.session.service.TokenBlacklistDomainService;
import org.xhy.community.domain.session.service.TokenIpMappingDomainService;
import org.xhy.community.domain.session.valueobject.ActiveIpInfo;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.query.UserQuery;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.infrastructure.exception.BusinessException;
import org.xhy.community.infrastructure.exception.UserErrorCode;
import org.xhy.community.interfaces.user.request.BlacklistQueryRequest;
import org.xhy.community.interfaces.user.request.DeviceSessionQueryRequest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员设备会话应用服务
 */
@Service
public class AdminDeviceSessionAppService {

    private static final Logger log = LoggerFactory.getLogger(AdminDeviceSessionAppService.class);
    private final UserDomainService userDomainService;
    private final DeviceSessionDomainService deviceSessionDomainService;
    private final TokenBlacklistDomainService tokenBlacklistDomainService;
    private final TokenIpMappingDomainService tokenIpMappingDomainService;

    public AdminDeviceSessionAppService(UserDomainService userDomainService,
                                      DeviceSessionDomainService deviceSessionDomainService,
                                      TokenBlacklistDomainService tokenBlacklistDomainService,
                                      TokenIpMappingDomainService tokenIpMappingDomainService) {
        this.userDomainService = userDomainService;
        this.deviceSessionDomainService = deviceSessionDomainService;
        this.tokenBlacklistDomainService = tokenBlacklistDomainService;
        this.tokenIpMappingDomainService = tokenIpMappingDomainService;
    }

    /**
     * 分页查询用户设备会话信息
     */
    public IPage<UserSessionSummaryDTO> queryUserSessions(DeviceSessionQueryRequest query) {
        // 构建用户查询条件
        UserQuery userQuery = new UserQuery();
        userQuery.setPageNum(query.getPageNum());
        userQuery.setPageSize(query.getPageSize());
        userQuery.setName(query.getUsername());

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

        // 获取该IP对应的所有tokens，并将它们加入黑名单
        Set<String> tokens = tokenIpMappingDomainService.getTokensByUserIp(userId, ip);
        if (!tokens.isEmpty()) {
            // 使用新的用户级黑名单管理
            tokenBlacklistDomainService.addUserToBlacklist(userId, tokens, null);
            // 移除token和IP的映射关系
            tokenIpMappingDomainService.removeTokensForUserIp(userId, ip);
        }

        // 强制下线设备
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

        // 获取该用户所有tokens，并将它们加入黑名单
        Set<String> tokens = tokenIpMappingDomainService.getAllTokensByUser(userId);
        if (!tokens.isEmpty()) {
            // 使用新的用户级黑名单管理
            tokenBlacklistDomainService.addUserToBlacklist(userId, tokens, null);
            // 移除所有token和IP的映射关系
            tokenIpMappingDomainService.removeAllTokensForUser(userId);
        }

        // 强制下线所有设备
        deviceSessionDomainService.clearAllActiveIps(userId);
    }

    /**
     * 获取Token黑名单统计信息
     */
    public TokenBlacklistStatsDTO getTokenBlacklistStats() {
        long count = tokenBlacklistDomainService.getBlacklistCount();
        String description = String.format("当前共有 %d 个token在黑名单中", count);
        return new TokenBlacklistStatsDTO(count, description);
    }

    /**
     * 分页查询被拉黑的用户
     */
    public IPage<BlacklistedUserDTO> getBlacklistedUsers(BlacklistQueryRequest request) {
        log.info("[黑名单查询] 开始查询被拉黑用户列表: pageNum={}, pageSize={}, username={}, email={}",
            request.getPageNum(), request.getPageSize(), request.getUsername(), request.getEmail());

        long offset = (long) (request.getPageNum() - 1) * request.getPageSize();

        // 1. 从Redis获取被拉黑的用户ID列表
        List<String> userIds = tokenBlacklistDomainService.getBlacklistedUserIds(offset, request.getPageSize());

        log.info("[黑名单查询] 从Redis获取到用户ID列表: userIds={}", userIds);

        if (userIds.isEmpty()) {
            log.warn("[黑名单查询] 没有找到被拉黑的用户");
            Page<BlacklistedUserDTO> emptyPage = new Page<>(request.getPageNum(), request.getPageSize());
            emptyPage.setTotal(0);
            emptyPage.setRecords(List.of());
            return emptyPage;
        }

        // 2. 批量查询用户信息
        List<UserEntity> users = userDomainService.getUsersByIds(userIds);
        Map<String, UserEntity> userMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user));

        // 3. 组装DTO
        List<BlacklistedUserDTO> blacklistedUsers = new ArrayList<>();
        for (String userId : userIds) {
            UserEntity user = userMap.get(userId);
            if (user != null) {
                // 应用搜索过滤条件
                boolean matches = true;
                if (StringUtils.hasText(request.getUsername()) &&
                    (user.getName() == null || !user.getName().contains(request.getUsername()))) {
                    matches = false;
                }
                if (StringUtils.hasText(request.getEmail()) &&
                    (user.getEmail() == null || !user.getEmail().contains(request.getEmail()))) {
                    matches = false;
                }

                if (matches) {
                    Long blacklistTime = tokenBlacklistDomainService.getUserBlacklistTime(userId);
                    Set<String> tokens = tokenBlacklistDomainService.getUserBlacklistTokens(userId);

                    BlacklistedUserDTO dto = new BlacklistedUserDTO();
                    dto.setUserId(userId);
                    dto.setUsername(user.getName());
                    dto.setEmail(user.getEmail());
                    dto.setBlacklistedAt(blacklistTime);
                    if (blacklistTime != null) {
                        dto.setBlacklistedTime(LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(blacklistTime), ZoneId.systemDefault()));
                    }
                    dto.setTokenCount(tokens.size());

                    blacklistedUsers.add(dto);
                }
            }
        }

        // 4. 构建分页结果
        Page<BlacklistedUserDTO> page = new Page<>(request.getPageNum(), request.getPageSize());
        page.setRecords(blacklistedUsers);
        page.setTotal(tokenBlacklistDomainService.getBlacklistedUserCount());

        log.info("[黑名单查询] 查询完成: 返回记录数={}, 总数={}", blacklistedUsers.size(), page.getTotal());

        return page;
    }

    /**
     * 移除指定用户的黑名单
     */
    public void removeUserFromBlacklist(String userId) {
        // 验证用户是否存在
        UserEntity user = userDomainService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 检查用户是否在黑名单中
        if (!tokenBlacklistDomainService.isUserBlacklisted(userId)) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND, "该用户未在黑名单中");
        }

        // 移除用户黑名单
        tokenBlacklistDomainService.removeUserFromBlacklist(userId);
    }
}