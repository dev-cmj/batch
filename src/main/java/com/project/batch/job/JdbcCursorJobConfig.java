package com.project.batch.job;

import com.project.batch.factory.JdbcReaderFactory;
import com.project.batch.model.Person;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 데이터베이스에서 커서(Cursor) 기법으로 데이터를 읽어오는 Job 입니다.
 * Reader, Processor, Writer는 모두 독립된 컴포넌트(Factory 또는 Component)를 통해 생성/주입받습니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JdbcCursorJobConfig {

    private final JdbcReaderFactory jdbcReaderFactory;
    private final ItemProcessor<Person, Person> jdbcCursorProcessor;
    private final ItemWriter<Person> personWriter; // 공통 Writer 주입

    @Bean
    public Job jdbcCursorJob(JobRepository jobRepository, @Qualifier("jdbcCursorStep") Step jdbcCursorStep) {
        return new JobBuilder("jdbcCursorJob", jobRepository)
                .start(jdbcCursorStep)
                .build();
    }

    @Bean
    public Step jdbcCursorStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
        return new StepBuilder("jdbcCursorStep", jobRepository)
                .<Person, Person>chunk(5, transactionManager)
                .reader(jdbcCursorReader())      // Reader는 Factory를 통해 생성
                .processor(jdbcCursorProcessor) // 주입받은 Processor 필드 사용
                .writer(personWriter)             // 주입받은 공통 Writer 필드 사용
                .build();
    }

    @Bean
    public JdbcCursorItemReader<Person> jdbcCursorReader() {
        return jdbcReaderFactory.createCursorReader(
                "jdbcCursorReader",
                Person.class,
                "SELECT id, first_name AS firstName, last_name AS lastName FROM person ORDER BY id" // 별칭 추가
        );
    }
}
