package com.minjeok4go.petplace.config;

// AsyncConfig.java
import org.springframework.context.annotation.*;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "recommendationExecutor")
    public ThreadPoolTaskExecutor recommendationExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);          // 기본 스레드 수
        ex.setMaxPoolSize(8);           // 최대 스레드 수
        ex.setQueueCapacity(500);       // 대기 큐
        ex.setThreadNamePrefix("rec-batch-");
        ex.setWaitForTasksToCompleteOnShutdown(true);
        ex.setAwaitTerminationSeconds(30);
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ex.initialize();
        return ex;
    }

    @Bean
    public Executor taskExecutor() {
        var ex = new org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(16);
        ex.setQueueCapacity(500);
        ex.setThreadNamePrefix("notification-");
        ex.initialize();
        return ex;
    }

    // @Configuration 클래스
    @Bean
    public TaskExecutor appTaskExecutor() {
        var t = new ThreadPoolTaskExecutor();
        t.setThreadNamePrefix("afterCommit-");
        t.setCorePoolSize(2);
        t.setMaxPoolSize(4);
        t.initialize();
        return t;
    }
}
