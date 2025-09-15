package com.project.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchJobExecutionListener implements JobExecutionListener {
    
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job Started: [{}] with parameters: {}", 
                jobExecution.getJobInstance().getJobName(), 
                jobExecution.getJobParameters());
    }
    
    @Override
    public void afterJob(JobExecution jobExecution) {

        log.info("Job Finished: [{}] Status: [{}]",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus());
        
        if (jobExecution.getStatus().isUnsuccessful()) {
            log.error("Job failed with errors: {}", 
                    jobExecution.getAllFailureExceptions());
        }
    }
}