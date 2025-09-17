package org.xhy.community.infrastructure.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xhy.community.domain.log.service.UserActivityLogDomainService;
import org.xhy.community.infrastructure.annotation.ActivityLog;
import org.xhy.community.infrastructure.util.activitylog.ActivityContext;
import org.xhy.community.infrastructure.util.activitylog.TargetInfo;
import org.xhy.community.infrastructure.util.activitylog.UrlPatternParser;
import org.xhy.community.infrastructure.config.UserContext;
import org.xhy.community.infrastructure.context.UserActivityContext;
import org.xhy.community.infrastructure.util.HttpRequestInfoExtractor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 业务活动日志切面
 * 处理@ActivityLog注解，自动解析URL并记录用户的业务操作行为
 * 
 * <p>核心功能：
 * <ul>
 *   <li>自动从URL解析目标类型和ID</li>
 *   <li>提取HTTP请求上下文信息</li>
 *   <li>异步记录活动日志，不影响主业务性能</li>
 *   <li>完善的错误处理和降级机制</li>
 * </ul>
 * 
 * <p>设计特点：
 * <ul>
 *   <li>极简使用：只需@ActivityLog(ActivityType.VIEW_POST)</li>
 *   <li>自动解析：无需手动指定目标类型和ID</li>  
 *   <li>高性能：异步处理，独立线程池</li>
 *   <li>可扩展：支持新的URL模式配置</li>
 * </ul>
 */
@Aspect
@Component
@Order(2) // 在UserActivityLogAspect之后执行，避免冲突
public class BusinessActivityLogAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(BusinessActivityLogAspect.class);
    
    private final UserActivityLogDomainService userActivityLogDomainService;
    private final ObjectMapper objectMapper;
    
    public BusinessActivityLogAspect(UserActivityLogDomainService userActivityLogDomainService,
                                   ObjectMapper objectMapper) {
        this.userActivityLogDomainService = userActivityLogDomainService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * 环绕通知：拦截标记了@ActivityLog注解的方法
     * 自动解析URL并记录用户的业务操作行为
     */
    @Around("@annotation(activityLog)")
    public Object logBusinessActivity(ProceedingJoinPoint joinPoint, ActivityLog activityLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取HTTP请求信息
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            logger.warn("No HTTP request found, skipping activity log");
            return joinPoint.proceed();
        }
        
        String requestPath = request.getRequestURI();
        
        // 自动解析目标信息
        TargetInfo targetInfo = UrlPatternParser.parseFromUrl(requestPath);
        
        // 提取用户活动上下文信息（浏览器、设备等）
        UserActivityContext userActivityContext = HttpRequestInfoExtractor.extractUserActivityContext();
        
        // 构建活动上下文
        ActivityContext context = ActivityContext.builder()
                .userId(getCurrentUserId(request))
                .activityType(activityLog.value())
                .targetType(targetInfo != null ? targetInfo.getType() : null)
                .targetId(targetInfo != null ? targetInfo.getId() : null)
                .requestMethod(request.getMethod())
                .requestPath(requestPath)
                .requestBody(activityLog.recordRequest() ? getRequestBody(joinPoint) : null)
                .ipAddress(getClientIp(request))
                .userAgent(request.getHeader("User-Agent"))
                .sessionId(getSessionId(request))
                .build();
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 记录成功日志
            if (activityLog.async()) {
                recordActivityAsync(context, userActivityContext, startTime, null);
            } else {
                recordActivity(context, userActivityContext, startTime, null);
            }
            
            return result;
            
        } catch (Exception e) {
            // 记录失败日志
            String errorMessage = e.getMessage();
            if (activityLog.async()) {
                recordActivityAsync(context, userActivityContext, startTime, errorMessage);
            } else {
                recordActivity(context, userActivityContext, startTime, errorMessage);
            }
            
            // 重新抛出异常，保持原有业务逻辑
            throw e;
        }
    }
    
    /**
     * 获取当前HTTP请求
     * 
     * @return HttpServletRequest对象，如果不在Web上下文中则返回null
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof ServletRequestAttributes) {
                return ((ServletRequestAttributes) requestAttributes).getRequest();
            }
        } catch (Exception e) {
            logger.warn("Failed to get current HTTP request: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取当前用户ID
     * 使用项目的UserContext来获取当前用户ID
     * 
     * @param request HTTP请求对象（保留参数以保持方法签名一致性）
     * @return 用户ID，如果未登录则返回null
     */
    private String getCurrentUserId(HttpServletRequest request) {
        try {
            // 使用项目统一的UserContext获取当前用户ID
            return UserContext.getCurrentUserId();
        } catch (IllegalStateException e) {
            // 用户未登录或未认证，返回null而不是抛异常
            logger.debug("Current user not authenticated: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.warn("Failed to get current user ID: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 获取请求体内容
     * 简化处理：将@RequestBody参数转为JSON
     * 
     * @param joinPoint 连接点
     * @return 请求体JSON字符串
     */
    private String getRequestBody(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }
        
        // 查找可能的@RequestBody参数
        for (Object arg : args) {
            if (arg != null && isRequestBodyObject(arg)) {
                try {
                    return objectMapper.writeValueAsString(arg);
                } catch (Exception e) {
                    logger.warn("Failed to serialize request body: {}", e.getMessage());
                    return "Failed to serialize";
                }
            }
        }
        
        return null;
    }
    
    /**
     * 判断是否为请求体对象
     * 
     * @param obj 待判断的对象
     * @return 是否为请求体对象
     */
    private boolean isRequestBodyObject(Object obj) {
        if (obj == null) {
            return false;
        }
        
        String className = obj.getClass().getSimpleName();
        // 根据命名约定判断是否为请求对象
        return className.endsWith("Request") || 
               className.endsWith("DTO") ||
               className.endsWith("Param");
    }
    
    /**
     * 获取客户端IP地址
     * 支持代理和负载均衡环境
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    /**
     * 获取会话ID
     * 
     * @param request HTTP请求对象
     * @return 会话ID
     */
    private String getSessionId(HttpServletRequest request) {
        try {
            return request.getSession().getId();
        } catch (Exception e) {
            logger.warn("Failed to get session ID: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 异步记录活动日志
     * 使用专用线程池确保日志记录不会影响主业务流程的性能
     * 
     * @param context 活动上下文
     * @param userActivityContext 用户活动上下文（包含浏览器、设备信息）
     * @param startTime 开始时间
     * @param errorMessage 错误信息，成功时为null
     */
    @Async("businessActivityExecutor")
    protected void recordActivityAsync(ActivityContext context, UserActivityContext userActivityContext, 
                                     long startTime, String errorMessage) {
        recordActivity(context, userActivityContext, startTime, errorMessage);
    }
    
    /**
     * 记录活动日志
     * 
     * @param context 活动上下文
     * @param userActivityContext 用户活动上下文（包含浏览器、设备信息）
     * @param startTime 开始时间
     * @param errorMessage 错误信息，成功时为null
     */
    private void recordActivity(ActivityContext context, UserActivityContext userActivityContext, 
                               long startTime, String errorMessage) {
        try {
            int executionTime = (int) (System.currentTimeMillis() - startTime);
            
            // 调用Domain服务记录业务活动，现在包含浏览器和设备信息
            userActivityLogDomainService.recordBusinessActivity(
                context.getUserId(),
                context.getActivityType(),
                context.getTargetType(),
                context.getTargetId(),
                context.getRequestMethod(),
                context.getRequestPath(),
                executionTime,
                context.getIpAddress(),
                context.getUserAgent(),
                context.getSessionId(),
                context.getRequestBody(),
                errorMessage,
                userActivityContext.getBrowser(),
                userActivityContext.getEquipment()
            );
            
            logger.debug("Successfully recorded business activity: userId={}, type={}, target={}:{}, executionTime={}ms", 
                        context.getUserId(), context.getActivityType(), 
                        context.getTargetType(), context.getTargetId(), executionTime);
                        
        } catch (Exception e) {
            // 日志记录失败不应该影响主业务，只记录错误日志
            logger.error("Failed to record business activity: userId={}, type={}, target={}:{}, error={}", 
                        context.getUserId(), context.getActivityType(), 
                        context.getTargetType(), context.getTargetId(), e.getMessage(), e);
        }
    }
}