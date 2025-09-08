package com.project.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Batch의 가장 기본적인 순차 처리(Sequential Processing) Job을 정의하는 설정 클래스입니다.
 * <p>
 * - Job: sequentialJob
 * - Step: sequentialStep
 * <p>
 * 이 Job은 메모리에 있는 간단한 데이터 리스트를 읽고(Read), 가공하여(Process), 로그로 출력(Write)하는
 * 가장 기본적인 Chunk 지향 처리 예제입니다.
 */
@Slf4j
@Configuration
public class SequentialJobConfig {

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
                .start(sequentialStep)
                .build();
    }

    /**
     * "sequentialStep"이라는 이름의 Batch Step을 생성합니다.
     * <p>
     * - chunk(3): Spring Batch의 핵심인 Chunk 지향 처리를 설정하는 부분입니다.
     *   - Reader가 3개의 아이템을 읽을 때까지 기다립니다.
     *   - 3개의 아이템이 모이면, 이 3개의 묶음(Chunk)을 Processor로 전달하여 하나씩 가공합니다.
     *   - 가공된 3개의 아이템 묶음을 Writer로 전달하여 한 번에 쓰기 처리를 합니다.
     *   - 이 모든 과정(읽기-처리-쓰기)은 하나의 트랜잭션 안에서 실행됩니다.
     *   - 만약 10개의 아이템이 있다면, (3개-3개-3개-1개)로 총 4번의 Chunk가 실행됩니다.
     *
     * @param jobRepository Step의 메타데이터를 관리하는 Repository
     * @param transactionManager Chunk 처리를 위한 트랜잭션 관리자
     * @return 생성된 Step
     */
    @Bean
    public Step sequentialStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sequentialStep", jobRepository)
                .<String, String>chunk(3, transactionManager)
                .reader(sequentialReader())
                .processor(sequentialProcessor())
                .writer(sequentialWriter())
                .build();
    }

    /**
     * Step에서 사용할 ItemReader를 생성합니다.
     * 이 Reader는 메모리에 있는 간단한 문자열 리스트를 읽습니다.
     * Bean 이름을 "sequentialReader"로 지정하여 다른 Job의 Reader와 충돌을 방지합니다.
     *
     * @return ListItemReader
     */
    @Bean
    public ListItemReader<String> sequentialReader() {
        List<String> data = Arrays.asList("item1", "item2", "item3", "item4", "item5", "item6", "item7", "item8", "item9", "item10");
        log.info("Reading data: {}", data);
        return new ListItemReader<>(data);
    }

    /**
     * Step에서 사용할 ItemProcessor를 생성합니다.
     * 이 Processor는 읽어온 각 아이템(문자열)을 대문자로 변환합니다.
     * Bean 이름을 "sequentialProcessor"로 지정하여 다른 Job의 Processor와 충돌을 방지합니다.
     *
     * @return ItemProcessor
     */
    @Bean
    public ItemProcessor<String, String> sequentialProcessor() {
        return item -> {
            String processedItem = item.toUpperCase();
            log.info("Processing {} -> {}", item, processedItem);
            return processedItem;
        };
    }

    /**
     * Step에서 사용할 ItemWriter를 생성합니다.
     * 이 Writer는 처리된 데이터 묶음(Chunk)을 로그로 출력합니다.
     * Bean 이름을 "sequentialWriter"로 지정하여 다른 Job의 Writer와 충돌을 방지합니다.
     *
     * @return ItemWriter
     */
    @Bean
    public ItemWriter<String> sequentialWriter() {
        return chunk -> {
            log.info("Writing chunk: {}", chunk.getItems());
        };
    }
}
