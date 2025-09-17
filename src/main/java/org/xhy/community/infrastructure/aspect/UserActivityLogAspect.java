package org.xhy.community.infrastructure.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xhy.community.infrastructure.annotation.LogUserActivity;
import org.xhy.community.infrastructure.context.UserActivityContext;
import org.xhy.community.infrastructure.util.HttpRequestInfoExtractor;
import org.xhy.community.domain.log.service.UserActivityLogDomainService;
import org.xhy.community.domain.common.valueobject.ActivityType;
import org.xhy.community.interfaces.user.request.LoginRequest;
import org.xhy.community.interfaces.user.request.RegisterRequest;
import org.xhy.community.infrastructure.config.ApiResponse;
import org.xhy.community.application.user.dto.LoginResponseDTO;
import org.xhy.community.application.user.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用户活动日志切面
 * 通过AOP方式自动记录用户活动日志，实现业务逻辑与日志记录的完全解耦
 */
@Aspect
@Component
@Order(1) // 设置切面执行顺序，确保在事务切面之前执行
public class UserActivityLogAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(UserActivityLogAspect.class);
    
    private final UserActivityLogDomainService userActivityLogDomainService;
    
    public UserActivityLogAspect(UserActivityLogDomainService userActivityLogDomainService) {
        this.userActivityLogDomainService = userActivityLogDomainService;
    }
    
    /**
     * 环绕通知：拦截标记了@LogUserActivity注解的方法
     */
    @Around("@annotation(logUserActivity)")
    public Object logUserActivity(ProceedingJoinPoint joinPoint, LogUserActivity logUserActivity) throws Throwable {
        // 提取请求上下文信息
        UserActivityContext context = HttpRequestInfoExtractor.extractUserActivityContext();
        
        // 从方法参数中提取邮箱
        String email = extractEmailFromArgs(joinPoint.getArgs());
        
        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            
            // 记录成功日志
            if (logUserActivity.logSuccess()) {
                String userId = extractUserIdFromResult(result);
                recordActivityAsync(
                    userId, 
                    email, 
                    logUserActivity.successType(), 
                    context, 
                    null // 成功时无失败原因
                );
            }
            
            return result;
            
        } catch (Exception e) {
            // 记录失败日志
            if (logUserActivity.logFailure()) {
                recordActivityAsync(
                    null, // 失败时用户ID为null
                    email, 
                    logUserActivity.failureType(), 
                    context, 
                    e.getMessage() // 异常信息作为失败原因
                );
            }
            
            // 重新抛出异常，保持原有业务逻辑
            throw e;
        }
    }
    
    /**
     * 从方法参数中提取邮箱
     * 支持 LoginRequest 和 RegisterRequest 类型
     */
    private String extractEmailFromArgs(Object[] args) {
        if (args == null || args.length == 0) {
            logger.warn("Method arguments is null or empty");
            return "unknown";
        }
        
        for (Object arg : args) {
            try {
                if (arg instanceof LoginRequest) {
                    String email = ((LoginRequest) arg).getEmail();
                    logger.debug("Extracted email from LoginRequest: {}", email);
                    return email != null ? email : "unknown";
                }
                if (arg instanceof RegisterRequest) {
                    String email = ((RegisterRequest) arg).getEmail();
                    logger.debug("Extracted email from RegisterRequest: {}", email);
                    return email != null ? email : "unknown";
                }
            } catch (Exception e) {
                logger.warn("Failed to extract email from argument: {}, error: {}", 
                           arg.getClass().getSimpleName(), e.getMessage());
            }
        }
        
        logger.warn("No email found in method arguments, available types: {}", 
                   java.util.Arrays.stream(args)
                           .map(arg -> arg.getClass().getSimpleName())
                           .collect(java.util.stream.Collectors.joining(", ")));
        return "unknown";
    }
    
    /**
     * 从方法返回结果中提取用户ID
     * 支持 ApiResponse<LoginResponseDTO> 和 ApiResponse<UserDTO> 类型
     */
    private String extractUserIdFromResult(Object result) {
        if (result == null) {
            return null;
        }
        
        try {
            if (result instanceof ApiResponse) {
                Object data = ((ApiResponse<?>) result).getData();
                if (data instanceof LoginResponseDTO) {
                    UserDTO user = ((LoginResponseDTO) data).getUser();
                    return user != null ? user.getId() : null;
                }
                if (data instanceof UserDTO) {
                    return ((UserDTO) data).getId();
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to extract userId from result: {}, error: {}", 
                       result.getClass().getSimpleName(), e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 异步记录用户活动日志
     * 使用专用线程池确保日志记录不会影响主业务流程的性能
     */
    @Async("userActivityLogExecutor")
    protected void recordActivityAsync(String userId, String email, ActivityType activityType,
                                   UserActivityContext context, String failureReason) {
        try {
            userActivityLogDomainService.recordActivity(
                userId,
                email,
                activityType,
                context.getBrowser(),
                context.getEquipment(),
                context.getIp(),
                context.getUserAgent(),
                failureReason
            );
            
            logger.debug("Successfully recorded user activity: email={}, type={}, ip={}", 
                        email, activityType, context.getIp());
                        
        } catch (Exception e) {
            // 日志记录失败不应该影响主业务，只记录警告日志
            logger.error("Failed to record user activity log: email={}, type={}, error={}", 
                        email, activityType, e.getMessage(), e);
        }
    }
}