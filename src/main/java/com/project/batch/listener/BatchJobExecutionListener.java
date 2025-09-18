package com.project.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class BatchJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        LocalDateTime startTime = LocalDateTime.now();

        log.info("======================================");
        log.info("JOB STARTED: [{}]", jobName);
        log.info("Start Time: {}", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("Parameters: {}", jobExecution.getJobParameters());

        if (jobName.contains("parallel")) {
            log.info("Processing Mode: PARALLEL (Multi-threading)");
        } else if (jobName.contains("sequential")) {
            log.info("Processing Mode: SEQUENTIAL (Single-threading)");
        }

        log.info("======================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(jobExecution.getStartTime(), endTime);

        log.info("======================================");
        log.info("JOB COMPLETED: [{}]", jobName);
        log.info("End Time: {}", endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("Total Duration: {} ms ({} seconds)", duration.toMillis(), duration.getSeconds());
        log.info("Status: {}", jobExecution.getStatus());

        // Step별 통계 정보
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            log.info("Step [{}] Stats:", stepExecution.getStepName());
            log.info("   ├─ Read Count: {}", stepExecution.getReadCount());
            log.info("   ├─ Write Count: {}", stepExecution.getWriteCount());
            log.info("   ├─ Commit Count: {}", stepExecution.getCommitCount());
            log.info("   ├─ Skip Count: {}", stepExecution.getSkipCount());
            log.info("   └─ Duration: {} ms", Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime()).toMillis());
        }

        // 성능 비교를 위한 메트릭
        if (jobName.contains("parallel")) {
            log.info("🔄 PARALLEL JOB METRICS:");
            log.info("   └─ Processed {} items in {} ms (Multi-threading)",
                    getTotalReadCount(jobExecution), duration.toMillis());
        } else if (jobName.contains("sequential")) {
            log.info("➡️ SEQUENTIAL JOB METRICS:");
            log.info("   └─ Processed {} items in {} ms (Single-threading)",
                    getTotalReadCount(jobExecution), duration.toMillis());
        }

        if (jobExecution.getStatus().isUnsuccessful()) {
            log.error("Job failed with errors: {}",
                    jobExecution.getAllFailureExceptions());
        } else {
            log.info("Job completed successfully!");
        }

        log.info("======================================");
    }

    private long getTotalReadCount(JobExecution jobExecution) {
        return jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getReadCount)
                .sum();
    }
}