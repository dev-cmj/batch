package com.project.batch.job;

import com.project.batch.model.Post;
import com.project.batch.reader.MockApiItemReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 외부 Mock API에서 대용량 데이터를 가져와 처리하는 Sequential Job을 정의하는 설정 클래스입니다.
 * <p>
 * - Job: sequentialJob
 * - Step: sequentialStep
 * <p>
 * 이 Job은 JSONPlaceholder API에서 Post 데이터를 페이징으로 읽고(Read), 가공하여(Process), 로그로 출력(Write)하는
 * 대용량 데이터 처리 예제입니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SequentialJobConfig {

    private final MockApiItemReader mockApiItemReader;

    /**
     * "sequentialJob"이라는 이름의 Batch Job을 생성합니다.
     *
     * @param jobRepository Job의 메타데이터를 관리하는 Repository
     * @param sequentialStep 이 Job에서 실행될 Step. Spring이 "sequentialStep"이라는 이름의 Bean을 찾아 주입합니다.
     * @return 생성된 Job
     */
    @Bean
    public Job sequentialJob(JobRepository jobRepository, Step sequentialStep) {
        return new JobBuilder("sequentialJob", jobRepository)
                .listener(new com.project.batch.listener.BatchJobExecutionListener())
                .start(sequentialStep)
                .build();
    }

    /**
     * "sequentialStep"이라는 이름의 Batch Step을 생성합니다.
     * <p>
     * - chunk(5): 외부 API에서 가져온 데이터를 5개씩 묶어서 처리합니다.
     *   - MockApiItemReader가 외부 API에서 Post 데이터를 페이징으로 읽어옵니다.
     *   - 5개의 Post가 모이면, 이 5개의 묶음(Chunk)을 Processor로 전달하여 하나씩 가공합니다.
     *   - 가공된 5개의 Post 묶음을 Writer로 전달하여 한 번에 쓰기 처리를 합니다.
     *   - 이 모든 과정(읽기-처리-쓰기)은 하나의 트랜잭션 안에서 실행됩니다.
     *
     * @param jobRepository Step의 메타데이터를 관리하는 Repository
     * @param transactionManager Chunk 처리를 위한 트랜잭션 관리자
     * @return 생성된 Step
     */
    @Bean
    public Step sequentialStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sequentialStep", jobRepository)
                .<Post, Post>chunk(5, transactionManager)
                .reader(mockApiItemReader)
                .processor(postProcessor())
                .writer(postWriter())
                .build();
    }

    /**
     * Step에서 사용할 ItemProcessor를 생성합니다.
     * 이 Processor는 읽어온 Post 데이터를 가공합니다.
     * - 제목을 대문자로 변환
     * - 본문을 50자로 제한
     *
     * @return ItemProcessor
     */
    @Bean
    public ItemProcessor<Post, Post> postProcessor() {
        return post -> {

            Post processedPost = Post.builder()
                    .id(post.getId())
                    .userId(post.getUserId())
                    .title(post.getProcessedTitle())
                    .body(post.getShortBody())
                    .build();

            log.info("Processing Post ID: {} -> Title: {}", post.getId(), processedPost.getTitle());
            return processedPost;
        };
    }

    /**
     * Step에서 사용할 ItemWriter를 생성합니다.
     * 이 Writer는 처리된 Post 데이터 묶음(Chunk)을 로그로 출력합니다.
     *
     * @return ItemWriter
     */
    @Bean
    public ItemWriter<Post> postWriter() {
        return chunk -> {
            log.info("Writing chunk of {} posts:", chunk.size());
            for (Post post : chunk.getItems()) {
                log.info("  - Post[{}]: {} (User: {})",
                    post.getId(), post.getTitle(), post.getUserId());
            }
            log.info("Chunk writing completed for {} posts", chunk.size());
        };
    }
}
