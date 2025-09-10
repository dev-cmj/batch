package com.project.batch.service;

import com.project.batch.job.QuartzBatchJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuartzSchedulerService {

    private final Scheduler scheduler;

    public void scheduleJob(String jobName, String groupName, String cronExpression) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(QuartzBatchJob.class)
                .withIdentity(jobName, groupName)
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "_trigger", groupName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        log.info("스케줄 등록 완료: {} - {}", jobName, cronExpression);
    }

    public void deleteJob(String jobName, String groupName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        scheduler.deleteJob(jobKey);
        log.info("스케줄 삭제 완료: {}", jobName);
    }

    public void pauseJob(String jobName, String groupName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        scheduler.pauseJob(jobKey);
        log.info("스케줄 일시정지: {}", jobName);
    }

    public void resumeJob(String jobName, String groupName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        scheduler.resumeJob(jobKey);
        log.info("스케줄 재개: {}", jobName);
    }
}