package com.project.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
public class TaskExecutorConfig {

    @Bean(name = "parallelTaskExecutor")
    public TaskExecutor parallelTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // CPU 코어 수 기반 동적 스레드 풀 크기 설정
        int cores = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cores);

        // 최대 스레드 풀 크기 (코어 수의 2배)
        executor.setMaxPoolSize(cores * 2);

        // 큐 용량
        executor.setQueueCapacity(50);

        // 스레드 이름 접두사
        executor.setThreadNamePrefix("ParallelBatch-");

        // 애플리케이션 종료 시 스레드 풀이 완료될 때까지 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 종료 대기 시간 (초)
        executor.setAwaitTerminationSeconds(30);

        // 스레드 풀 초기화
        executor.initialize();

        log.info("Parallel TaskExecutor configured: CorePool={}, MaxPool={}, QueueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }

    @Bean(name = "heavyTaskExecutor")
    public TaskExecutor heavyTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 대용량 처리를 위한 CPU 코어 기반 스레드 풀
        int cores = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cores * 2);
        executor.setMaxPoolSize(cores * 4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("HeavyBatch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Heavy TaskExecutor configured: CorePool={}, MaxPool={}, QueueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}