package org.xhy.community.application.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.xhy.community.application.user.assembler.AdminUserAssembler;
import org.xhy.community.application.user.dto.AdminUserDTO;
import org.xhy.community.domain.user.entity.UserEntity;
import org.xhy.community.domain.user.query.UserQuery;
import org.xhy.community.domain.user.service.UserDomainService;
import org.xhy.community.domain.subscription.entity.UserSubscriptionEntity;
import org.xhy.community.domain.subscription.entity.SubscriptionPlanEntity;
import org.xhy.community.domain.subscription.service.SubscriptionDomainService;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.interfaces.user.request.AdminUserQueryRequest;
import org.xhy.community.interfaces.user.request.UpdateUserDevicesRequest;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员用户应用服务
 * 处理管理员对用户的管理操作
 */
@Service
public class AdminUserAppService {

    private final UserDomainService userDomainService;
    private final SubscriptionDomainService subscriptionDomainService;
    private final SubscriptionPlanDomainService subscriptionPlanDomainService;

    public AdminUserAppService(UserDomainService userDomainService,
                               SubscriptionDomainService subscriptionDomainService,
                               SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.userDomainService = userDomainService;
        this.subscriptionDomainService = subscriptionDomainService;
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }
    
    /**
     * 分页查询用户列表
     * 支持邮箱、昵称、状态条件查询
     *
     * @param request 查询请求参数
     * @return 用户分页列表（包含当前套餐名称）
     */
    public IPage<AdminUserDTO> queryUsers(AdminUserQueryRequest request) {
        UserQuery query = AdminUserAssembler.fromQueryRequest(request);
        IPage<UserEntity> userPage = userDomainService.queryUsers(query);

        // 如果没有用户数据，直接返回空结果
        if (userPage.getRecords().isEmpty()) {
            return AdminUserAssembler.toDTOPage(userPage);
        }

        // 提取所有用户ID
        Set<String> userIds = userPage.getRecords().stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());

        // 批量查询每个用户的当前有效订阅
        Map<String, UserSubscriptionEntity> userSubscriptionMap =
                subscriptionDomainService.getUsersActiveSubscriptions(userIds);

        // 提取所有订阅套餐ID
        Set<String> planIds = userSubscriptionMap.values().stream()
                .map(UserSubscriptionEntity::getSubscriptionPlanId)
                .collect(Collectors.toSet());

        // 批量查询所有套餐信息
        Map<String, SubscriptionPlanEntity> planMap = planIds.isEmpty()
                ? Map.of()
                : subscriptionPlanDomainService.getSubscriptionPlansByIds(planIds)
                        .stream()
                        .collect(Collectors.toMap(SubscriptionPlanEntity::getId, plan -> plan));

        // 构建用户ID到套餐名称的映射
        Map<String, String> userPlanNameMap = userSubscriptionMap.entrySet().stream()
                .filter(entry -> planMap.containsKey(entry.getValue().getSubscriptionPlanId()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> planMap.get(entry.getValue().getSubscriptionPlanId()).getName()
                ));

        return AdminUserAssembler.toDTOPage(userPage, userPlanNameMap);
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
    
    /**
     * 更新用户设备数量
     * 修改用户最大并发设备数量
     * 
     * @param userId 用户ID
     * @param request 设备数量更新请求
     * @return 更新后的用户信息
     */
    public AdminUserDTO updateUserDevices(String userId, UpdateUserDevicesRequest request) {
        UserEntity user = userDomainService.updateUserSettings(userId, null, request.getMaxConcurrentDevices());
        return AdminUserAssembler.toDTO(user);
    }
}