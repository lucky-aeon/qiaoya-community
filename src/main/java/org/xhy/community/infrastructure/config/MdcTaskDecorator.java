package org.xhy.community.infrastructure.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 将父线程的 MDC 上下文复制到子线程，确保 @Async 等异步日志包含 requestId/userId 等字段。
 */
public class MdcTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        // 复制调用方线程的 MDC 上下文
        final Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            // 备份子线程原有 MDC（如有）以便恢复
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                } else {
                    MDC.clear();
                }
                runnable.run();
            } finally {
                // 恢复子线程原有 MDC，避免污染线程池中的其他任务
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        };
    }
}

