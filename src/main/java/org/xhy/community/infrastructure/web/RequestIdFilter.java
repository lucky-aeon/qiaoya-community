package org.xhy.community.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.xhy.community.infrastructure.util.ClientIpUtil;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Request ID 追踪过滤器
 *
 * 功能：
 * 1. 从请求头获取或生成 Request ID
 * 2. 将 Request ID、IP、URI 写入 MDC
 * 3. 在响应头中返回 Request ID
 * 4. 清理 MDC 避免内存泄漏
 *
 * 优先级：最高（Order = -100），在所有拦截器之前执行
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_IP = "ip";
    private static final String MDC_URI = "uri";
    private static final String MDC_START_AT = "requestStartAt";
    private static final String MDC_END_AT = "requestEndAt";
    private static final String MDC_DURATION_MS = "durationMs";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();
        String startAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        try {
            // 1. 获取或生成 Request ID
            String requestId = getOrGenerateRequestId(request);

            // 2. 写入 MDC
            MDC.put(MDC_REQUEST_ID, requestId);
            MDC.put(MDC_IP, ClientIpUtil.getClientIp(request));
            // URI 包含查询参数，便于定位
            String uri = request.getRequestURI();
            String qs = request.getQueryString();
            if (StringUtils.hasText(qs)) {
                uri = uri + "?" + qs;
            }
            MDC.put(MDC_URI, uri);
            MDC.put(MDC_START_AT, startAt);

            // 3. 设置响应头
            response.setHeader(REQUEST_ID_HEADER, requestId);

            // 4. 继续执行过滤链
            filterChain.doFilter(request, response);
        } finally {
            // 5. 统一输出一条访问日志，串起本次请求
            String uri = MDC.get(MDC_URI);
            String userId = MDC.get("userId"); // 由 UserContextInterceptor 写入
            long end = System.currentTimeMillis();
            long cost = end - start;
            String endAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            MDC.put(MDC_END_AT, endAt);
            MDC.put(MDC_DURATION_MS, String.valueOf(cost));
            log.info("HTTP {} {} -> {} ({}ms) userId={}", request.getMethod(), uri, response.getStatus(), cost, userId);

            // 5. 清理 MDC（确保不会内存泄漏）
            MDC.clear();
        }
    }

    /**
     * 获取或生成 Request ID
     * 优先使用客户端传入的 Request ID，如果没有则生成新的
     */
    private String getOrGenerateRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);

        if (StringUtils.hasText(requestId)) {
            return requestId;
        }

        // 生成 UUID 作为 Request ID
        return UUID.randomUUID().toString();
    }
}
