package com.project.batch.scheduler;

import lombok.Getter;
import lombok.Setter;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobLocator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@DisallowConcurrentExecution
@Getter
@Setter
public class BatchJobLauncher extends QuartzJobBean {

    private String jobName;
    private String jobParam;

    @Autowired
    private JobLauncher jobLauncher;
    
    @Autowired
    private JobLocator jobLocator;

    public BatchJobLauncher() {
    }

    public BatchJobLauncher(JobLauncher jobLauncher, JobLocator jobLocator) {
        this.jobLauncher = jobLauncher;
        this.jobLocator = jobLocator;
    }

    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            Job job = jobLocator.getJob(jobName);

            /////////////////////////////////////////////
            // 단건 처리에 대해 executeScheduler가 실행되면.. TaskId 최초 생성.
            // 스케쥴 처리 이전에 taskID 없으면 여기서 최초 생성 또는 단건에서 triggerFired에서 잡 호출 전에 context에 TaskId 주입
            // 아래에 셋팅 후 SchedulerJobListener:jobToBeExecuted에서 잡 호출
            String taskId = context.getMergedJobDataMap().getString("TaskId");
            if (taskId == null || taskId.isEmpty()) {
                taskId = UUID.randomUUID().toString();
            }
            JobParameters params = new JobParametersBuilder()
                    .addString("JobID", jobName + "-" + System.currentTimeMillis())
                    .addString("JobParam", jobParam)
                    .addString("TaskId", taskId)
                    .toJobParameters();

            jobLauncher.run(job, params);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
