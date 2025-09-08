package com.project.batch.job;

import com.project.batch.model.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
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
import org.springframework.transaction.PlatformTransactionManager;

/**
 * MyBatis와 Spring Batch를 연동하여 페이징 처리를 수행하는 Job 입니다.
 * Processor와 Writer는 독립된 컴포넌트로 분리하여 주입받습니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MyBatisPagingJobConfig {

    private final SqlSessionFactory sqlSessionFactory;

    @Bean
    public Job mybatisPagingJob(JobRepository jobRepository, @Qualifier("mybatisPagingStep") Step mybatisPagingStep) {
        return new JobBuilder("mybatisPagingJob", jobRepository)
                .start(mybatisPagingStep)
                .build();
    }

    @Bean
    public Step mybatisPagingStep(JobRepository jobRepository,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("mybatisPagingStep", jobRepository)
                .<Person, Person>chunk(5, transactionManager)
                .reader(mybatisPagingReader())       // Reader는 이 클래스 내에서 생성
                .processor(mybatisPagingProcessor()) // 주입받은 Processor 필드 사용
                .writer(mybatisPagingWriter())              // 주입받은 공통 Writer 필드 사용
                .build();
    }

    @Bean
    public MyBatisPagingItemReader<Person> mybatisPagingReader() {
        return new MyBatisPagingItemReaderBuilder<Person>()
                .sqlSessionFactory(sqlSessionFactory)
                .queryId("com.project.batch.mapper.PersonMapper.findByPaging")
                .pageSize(5)
                .build();
    }

    @Bean
    public ItemWriter<Person> mybatisPagingWriter() {
        return items -> {
            for (Person item : items) {
                log.info("MyBatis Paging Writer - Person Name: {}", item.getFirstName() + " " + item.getLastName());
            }
        };
    }

    @Bean
    public ItemProcessor<Person, Person> mybatisPagingProcessor() {
        return item -> {
            item.setFirstName(item.getFirstName().toUpperCase());
            item.setLastName(item.getLastName().toUpperCase());
            return item;
        };
    }
}
