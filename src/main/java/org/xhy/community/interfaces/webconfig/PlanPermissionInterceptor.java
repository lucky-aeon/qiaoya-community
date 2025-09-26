package org.xhy.community.interfaces.webconfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.xhy.community.application.permission.service.UserPermissionAppService;
import org.xhy.community.infrastructure.annotation.RequiresPlanPermissions;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 前台接口功能权限拦截器
 * - 仅对标注了 @RequiresPlanPermissions 的接口进行权限校验；
 * - 安全边界在后端：即使前端不做任何权限码判断，也会在此处被拒绝（403）。
 * - 与前端返回的权限码无关（那只是为了更友好的按钮显示体验）。
 */
@Component
public class PlanPermissionInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(PlanPermissionInterceptor.class);

    private final UserPermissionAppService userPermissionAppService;

    public PlanPermissionInterceptor(UserPermissionAppService userPermissionAppService) {
        this.userPermissionAppService = userPermissionAppService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 先查方法注解，其次类注解
        RequiresPlanPermissions ann = getAnnotation(handlerMethod);
        if (ann == null || (ann.value().length == 0 && ann.items().length == 0)) {
            return true; // 未要求功能权限，直接放行
        }

        String userId = org.xhy.community.infrastructure.config.UserContext.getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Set<String> need = new HashSet<>(Arrays.asList(ann.value()));
        if (ann.items().length > 0) {
            for (RequiresPlanPermissions.Item it : ann.items()) {
                if (it != null && it.code() != null && !it.code().isBlank()) {
                    need.add(it.code());
                }
            }
        }
        Set<String> have = new HashSet<>(userPermissionAppService.getUserPlanPermissionCodes(userId));
        boolean allowed = have.containsAll(need);

        if (!allowed) {
            log.warn("功能权限不足：userId={}, need={}, have={} path={}", userId, need, have, request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    private RequiresPlanPermissions getAnnotation(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        RequiresPlanPermissions ann = method.getAnnotation(RequiresPlanPermissions.class);
        if (ann != null) return ann;
        Class<?> clazz = handlerMethod.getBeanType();
        return clazz.getAnnotation(RequiresPlanPermissions.class);
    }
}
