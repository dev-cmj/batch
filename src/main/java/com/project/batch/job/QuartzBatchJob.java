package com.project.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzBatchJob implements Job {

    private final JobLauncher jobLauncher;
    private final org.springframework.batch.core.Job jdbcPagingJob;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("Quartz에서 배치 작업 시작: {}", context.getJobDetail().getKey());
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(jdbcPagingJob, jobParameters);
            
            log.info("Quartz 배치 작업 완료: {}", context.getJobDetail().getKey());
        } catch (Exception e) {
            log.error("Quartz 배치 작업 실행 중 오류 발생", e);
            throw new JobExecutionException("배치 작업 실행 실패", e);
        }
    }
}