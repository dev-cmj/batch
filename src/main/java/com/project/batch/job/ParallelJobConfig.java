package com.project.batch.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.project.batch.listener.BatchJobExecutionListener;
import com.project.batch.model.Post;
import com.project.batch.reader.PartitionedMockApiItemReader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파티셔닝 기반 병렬 처리 Job 설정 클래스입니다.
 * <p>
 * - Job: partitionedJob (Partitioning 기반 병렬 처리)
 * <p>
 * 데이터를 파티션별로 분할하여 완전 독립적인 병렬 처리를 수행합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ParallelJobConfig {


    /**
     * 파티셔닝 기반 병렬처리 Step을 생성합니다.
     * 데이터를 범위별로 분할하여 여러 Worker에서 독립적으로 처리합니다.
     */
    @Bean
    public Step partitionedStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               @Qualifier("parallelTaskExecutor") TaskExecutor taskExecutor,
                               PartitionedMockApiItemReader partitionedMockApiItemReader) {
        return new StepBuilder("partitionedStep", jobRepository)
                .partitioner("workerStep", partitioner())
                .step(workerStep(jobRepository, transactionManager, partitionedMockApiItemReader))
                .gridSize(4) // 4개의 파티션으로 분할
                .taskExecutor(taskExecutor)
                .build();
    }

    /**
     * Worker Step - 각 파티션을 처리하는 실제 Step
     */
    @Bean
    public Step workerStep(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          PartitionedMockApiItemReader partitionedMockApiItemReader) {
        return new StepBuilder("workerStep", jobRepository)
                .<Post, Post>chunk(5, transactionManager)
                .reader(partitionedMockApiItemReader)
                .processor(partitionedPostProcessor())
                .writer(partitionedPostWriter())
                .build();
    }

    /**
     * 파티셔너 - 데이터를 범위별로 분할
     */
    @Bean
    public Partitioner partitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> partitionMap = new HashMap<>();

            int totalPages = 1000; // maxPages (10,000 items / 10 per page)
            int pagesPerPartition = (int) Math.ceil((double) totalPages / gridSize);

            for (int i = 0; i < gridSize; i++) {
                ExecutionContext executionContext = new ExecutionContext();

                int startPage = (i * pagesPerPartition) + 1;
                int endPage = Math.min(startPage + pagesPerPartition - 1, totalPages);

                executionContext.putInt("startPage", startPage);
                executionContext.putInt("endPage", endPage);
                executionContext.putInt("partitionId", i);

                partitionMap.put("partition" + i, executionContext);

                log.info("Created partition{}: pages {}-{}", i, startPage, endPage);
            }

            return partitionMap;
        };
    }


    /**
     * 파티션별 ItemProcessor
     */
    @Bean
    public ItemProcessor<Post, Post> partitionedPostProcessor() {
        return post -> {
            String threadName = Thread.currentThread().getName();
            long startTime = System.currentTimeMillis();

            Post processedPost = Post.builder()
                    .id(post.getId())
                    .userId(post.getUserId())
                    .title("[PARTITIONED] " + post.getProcessedTitle())
                    .body(post.getShortBody() + " [Processed by: " + threadName + "]")
                    .build();

            long endTime = System.currentTimeMillis();
            log.info("Partitioned Processing Post ID: {} on {} (took {}ms)",
                    post.getId(), threadName, (endTime - startTime));

            return processedPost;
        };
    }

    /**
     * 파티션별 ItemWriter
     */
    @Bean
    public ItemWriter<Post> partitionedPostWriter() {
        return chunk -> {
            String threadName = Thread.currentThread().getName();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));

            log.info("=== PARTITIONED WRITING ===");
            log.info("Thread: {} | Time: {} | Chunk size: {}", threadName, timestamp, chunk.size());

            // 파티션별 독립적인 배치 쓰기 처리
            for (Post post : chunk.getItems()) {
                log.info("  └─ Partition Post[{}]: {} (User: {})",
                        post.getId(), post.getTitle(), post.getUserId());
            }

            log.info("Partitioned chunk writing completed by {} (Total: {} posts)",
                    threadName, chunk.size());
            log.info("===============================");
        };
    }

    /**
     * 파티셔닝 Job 추가
     */
    @Bean
    public Job partitionedJob(JobRepository jobRepository, Step partitionedStep) {
        return new JobBuilder("partitionedJob", jobRepository)
                .listener(new BatchJobExecutionListener())
                .start(partitionedStep)
                .build();
    }

}