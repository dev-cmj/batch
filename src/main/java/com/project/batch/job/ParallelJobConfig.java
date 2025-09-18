package com.project.batch.job;

import com.project.batch.model.Post;
import com.project.batch.reader.ParallelMockApiItemReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 외부 Mock API에서 대용량 데이터를 병렬로 처리하는 Parallel Job을 정의하는 설정 클래스입니다.
 * <p>
 * - Job: parallelJob
 * - Step: parallelStep
 * <p>
 * 이 Job은 Multi-threading을 사용하여 JSONPlaceholder API에서 Post 데이터를 병렬로 처리합니다.
 * 순차처리 Job과 성능을 비교할 수 있도록 설계되었습니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ParallelJobConfig {

    private final ParallelMockApiItemReader parallelMockApiItemReader;

    @Bean
    public Job parallelJob(JobRepository jobRepository, @Qualifier("parallelStep") Step parallelStep) {
        return new JobBuilder("parallelJob", jobRepository)
                .listener(new com.project.batch.listener.BatchJobExecutionListener())
                .start(parallelStep)
                .build();
    }

    /**
     * 병렬처리 Step을 생성합니다.
     * <p>
     * - chunk(3): 3개씩 묶어서 처리 (순차처리보다 작은 청크로 더 빠른 처리)
     * - taskExecutor: 병렬처리를 위한 TaskExecutor 사용
     * - throttleLimit: 동시 실행 스레드 수 제한
     */
    @Bean
    public Step parallelStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           @Qualifier("parallelTaskExecutor") TaskExecutor taskExecutor) {
        return new StepBuilder("parallelStep", jobRepository)
                .<Post, Post>chunk(3, transactionManager)
                .reader(parallelMockApiItemReader.createSynchronizedReader())
                .processor(parallelPostProcessor())
                .writer(parallelPostWriter())
                .taskExecutor(taskExecutor)
                .build();
    }

    /**
     * 병렬처리용 ItemProcessor를 생성합니다.
     * 스레드 안전성을 고려하여 상태를 공유하지 않는 stateless 처리기입니다.
     */
    @Bean
    public ItemProcessor<Post, Post> parallelPostProcessor() {
        return post -> {
            if (post == null) {
                return null;
            }

            String threadName = Thread.currentThread().getName();
            long startTime = System.currentTimeMillis();

            // 병렬처리 시뮬레이션을 위한 약간의 처리 지연
            try {
                Thread.sleep(100); // 100ms 처리 시간 시뮬레이션
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Processing interrupted for post {}", post.getId());
            }

            Post processedPost = Post.builder()
                    .id(post.getId())
                    .userId(post.getUserId())
                    .title("[PARALLEL] " + post.getProcessedTitle())
                    .body(post.getShortBody() + " [Processed by: " + threadName + "]")
                    .build();

            long endTime = System.currentTimeMillis();
            log.info("Parallel Processing Post ID: {} on {} (took {}ms)",
                    post.getId(), threadName, (endTime - startTime));

            return processedPost;
        };
    }

    /**
     * 병렬처리용 ItemWriter를 생성합니다.
     * 스레드 안전성을 위해 synchronized 블록을 사용합니다.
     */
    @Bean
    public ItemWriter<Post> parallelPostWriter() {
        return chunk -> {
            String threadName = Thread.currentThread().getName();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));

            // 동시 쓰기 시 로그 충돌 방지를 위한 동기화
            synchronized (this) {
                log.info("=== PARALLEL WRITING ===");
                log.info("Thread: {} | Time: {} | Chunk size: {}", threadName, timestamp, chunk.size());

                for (Post post : chunk.getItems()) {
                    log.info("  └─ Post[{}]: {} (User: {})",
                            post.getId(), post.getTitle(), post.getUserId());
                }

                log.info("Parallel chunk writing completed by {} (Total: {} posts)",
                        threadName, chunk.size());
                log.info("========================");
            }
        };
    }
}