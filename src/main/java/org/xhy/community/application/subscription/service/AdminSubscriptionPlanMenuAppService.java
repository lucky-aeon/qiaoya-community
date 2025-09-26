package org.xhy.community.application.subscription.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xhy.community.application.subscription.dto.MenuOptionDTO;
import org.xhy.community.domain.subscription.service.SubscriptionPlanDomainService;
import org.xhy.community.interfaces.subscription.request.UpdateSubscriptionPlanMenusRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminSubscriptionPlanMenuAppService {

    private final SubscriptionPlanDomainService subscriptionPlanDomainService;

    public AdminSubscriptionPlanMenuAppService(SubscriptionPlanDomainService subscriptionPlanDomainService) {
        this.subscriptionPlanDomainService = subscriptionPlanDomainService;
    }

    public List<MenuOptionDTO> getMenuOptions() {
        // 固定清单（与前端约定）：按“名称 / 分组 / 路径”
        return List.of(
            new MenuOptionDTO("MENU_DASHBOARD_HOME", "首页", "导航", "/dashboard/home"),
            new MenuOptionDTO("MENU_DASHBOARD_DISCUSSIONS", "讨论", "导航", "/dashboard/discussions"),
            new MenuOptionDTO("MENU_DASHBOARD_COURSES", "课程", "导航", "/dashboard/courses"),
            new MenuOptionDTO("MENU_DASHBOARD_CHANGELOG", "更新日志", "导航", "/dashboard/changelog"),
            new MenuOptionDTO("MENU_USER_BACKEND", "用户中心", "入口", "/dashboard/user-backend"),
            new MenuOptionDTO("MENU_USER_ARTICLES", "我的文章", "用户中心", "/dashboard/user-backend/articles"),
            new MenuOptionDTO("MENU_USER_COMMENTS", "我的评论", "用户中心", "/dashboard/user-backend/comments"),
            new MenuOptionDTO("MENU_USER_TESTIMONIAL", "我的评价", "用户中心", "/dashboard/user-backend/testimonial"),
            new MenuOptionDTO("MENU_USER_RESOURCES", "资源管理", "用户中心", "/dashboard/user-backend/resources"),
            new MenuOptionDTO("MENU_USER_MESSAGES", "消息中心", "用户中心", "/dashboard/user-backend/messages"),
            new MenuOptionDTO("MENU_USER_FOLLOWS", "关注管理", "用户中心", "/dashboard/user-backend/follows"),
            new MenuOptionDTO("MENU_USER_PROFILE", "个人信息", "用户中心", "/dashboard/user-backend/profile"),
            new MenuOptionDTO("MENU_USER_DEVICES", "设备管理", "用户中心", "/dashboard/user-backend/devices"),
            new MenuOptionDTO("MENU_MEMBERSHIP", "会员与套餐", "公开入口", "/dashboard/membership"),
            new MenuOptionDTO("MENU_REDEEM_CDK", "CDK 激活", "公开入口", "/dashboard/redeem")
        );
    }

    public List<String> getSubscriptionPlanMenuCodes(String planId) {
        return subscriptionPlanDomainService.getSubscriptionPlanMenuCodes(planId);
    }

    @Transactional
    public void updateSubscriptionPlanMenus(String planId, UpdateSubscriptionPlanMenusRequest request) {
        List<String> menus = request == null ? List.of() : (request.getMenus() == null ? List.of() : request.getMenus());
        // 去重、去空白
        List<String> normalized = menus.stream()
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
        subscriptionPlanDomainService.syncSubscriptionPlanMenus(planId, normalized);
    }
}
