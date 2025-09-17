package org.xhy.community.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步执行配置
 * 配置专门用于用户活动日志记录的线程池
 */
@Configuration
public class AsyncConfig {
    
    /**
     * 配置用于异步日志记录的线程池
     * 使用独立的线程池确保日志记录不会影响主业务性能
     */
    @Bean("userActivityLogExecutor")
    public Executor userActivityLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：2个线程足够处理日志记录任务
        executor.setCorePoolSize(2);
        
        // 最大线程数：在高负载时最多4个线程
        executor.setMaxPoolSize(4);
        
        // 队列容量：100个任务的缓冲队列
        executor.setQueueCapacity(100);
        
        // 线程空闲时间：60秒后回收多余线程
        executor.setKeepAliveSeconds(60);
        
        // 线程名前缀：便于监控和调试
        executor.setThreadNamePrefix("UserActivityLog-");
        
        // 拒绝策略：日志记录失败时直接丢弃，不影响主业务
        executor.setRejectedExecutionHandler((r, executor1) -> {
            System.err.println("User activity log task rejected, queue is full");
        });
        
        // 等待任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        
        executor.initialize();
        return executor;
    }
    
    /**
     * 默认的异步执行器
     * 为其他异步任务提供通用的线程池
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}