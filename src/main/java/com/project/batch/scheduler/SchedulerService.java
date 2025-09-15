package com.project.batch.scheduler;

import com.project.batch.constants.BatchConstants;
import com.project.batch.exception.BatchException;
import com.project.batch.repository.SchedulerRepository;
import com.project.batch.vo.SchedulerVo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final ApplicationContext context;
    private final Scheduler scheduler;
    private final SchedulerJobCreator schedulerJobCreator;
    private final SchedulerRepository schedulerRepository;

    @PostConstruct
    public void init() {
        startAllSchedulers();
    }

    public void startAllSchedulers() {
        log.info("Starting all schedulers - Scheduler Name: {}", getSchedulerName());
        
        List<SchedulerVo> schedulerList = getActiveSchedulerList();
        if (CollectionUtils.isEmpty(schedulerList)) {
            log.warn("No active schedule information found");
            return;
        }

        List<String> successJobs = new ArrayList<>();
        List<String> failedJobs = new ArrayList<>();

        for (SchedulerVo scheduleInfo : schedulerList) {
            try {
                scheduleJob(scheduleInfo);
                successJobs.add(scheduleInfo.getJobName());
                log.info("Successfully scheduled job: {}", scheduleInfo.getJobName());
            } catch (Exception e) {
                failedJobs.add(scheduleInfo.getJobName());
                log.error("Failed to schedule job: {} - Error: {}", scheduleInfo.getJobName(), e.getMessage(), e);
            }
        }

        logSchedulingResults(successJobs, failedJobs);
    }

    public void scheduleJob(SchedulerVo scheduleInfo) throws SchedulerException {
        JobDataMap jobDataMap = createJobDataMap(scheduleInfo);
        JobDetail jobDetail = schedulerJobCreator.createJob(BatchJobLauncher.class, true, context, jobDataMap);
        Trigger trigger = createTrigger(scheduleInfo);

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (ObjectAlreadyExistsException oae) {
            log.warn("Job already exists, skipping: {} - {}", scheduleInfo.getJobName(), oae.getMessage());
            handleExistingJob(scheduleInfo, jobDetail, trigger);
        }
    }

    private JobDataMap createJobDataMap(SchedulerVo scheduleInfo) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobName", scheduleInfo.getJobName());
        jobDataMap.put("jobParam", scheduleInfo.getJobParam());
        return jobDataMap;
    }

    private Trigger createTrigger(SchedulerVo scheduleInfo) {
        if ("cron".equalsIgnoreCase(scheduleInfo.getJobType())) {
            return createCronTrigger(scheduleInfo);
        } else {
            return createSimpleTrigger(scheduleInfo);
        }
    }

    private Trigger createCronTrigger(SchedulerVo scheduleInfo) {
        String cronExpression = scheduleInfo.getCronExpression();
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new BatchException(BatchConstants.ErrorCodes.VALIDATION_ERROR, 
                "Invalid cron expression for job: " + scheduleInfo.getJobName() + " - " + cronExpression);
        }
        return schedulerJobCreator.createCronTrigger(scheduleInfo.getTriggerName(),
                cronExpression, CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW);
    }

    private Trigger createSimpleTrigger(SchedulerVo scheduleInfo) {
        return schedulerJobCreator.createSimpleTrigger(scheduleInfo.getTriggerName(),
                Math.toIntExact(scheduleInfo.getRepeatInterval()), SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
    }

    private void handleExistingJob(SchedulerVo scheduleInfo, JobDetail jobDetail, Trigger trigger) throws SchedulerException {
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleInfo.getTriggerName());
        if (scheduler.checkExists(triggerKey)) {
            scheduler.rescheduleJob(triggerKey, trigger);
            log.info("Rescheduled existing job: {}", scheduleInfo.getJobName());
        }
    }

    private List<SchedulerVo> getActiveSchedulerList() {
        return schedulerRepository.findAllActive();
    }

    private String getSchedulerName() {
        try {
            return scheduler.getSchedulerName();
        } catch (SchedulerException e) {
            log.error("Failed to get scheduler name", e);
            return "UNKNOWN";
        }
    }

    private void logSchedulingResults(List<String> successJobs, List<String> failedJobs) {
        log.info("Scheduler startup completed - Success: {}, Failed: {}", 
                successJobs.size(), failedJobs.size());
        
        if (!successJobs.isEmpty()) {
            log.info("Successfully scheduled jobs: {}", successJobs);
        }
        
        if (!failedJobs.isEmpty()) {
            log.error("Failed to schedule jobs: {}", failedJobs);
        }
    }

    public void stopAllSchedulers() {
        try {
            log.info("Stopping all schedulers");
            scheduler.standby();
            log.info("All schedulers stopped successfully");
        } catch (SchedulerException e) {
            log.error("Failed to stop schedulers", e);
            throw new BatchException(BatchConstants.ErrorCodes.JOB_EXECUTION_ERROR, 
                "Failed to stop schedulers", e);
        }
    }

    public void pauseJob(String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName);
        scheduler.pauseJob(jobKey);
        log.info("Paused job: {}", jobName);
    }

    public void resumeJob(String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName);
        scheduler.resumeJob(jobKey);
        log.info("Resumed job: {}", jobName);
    }

    public boolean isJobRunning(String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName);
        List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
        return executingJobs.stream()
                .anyMatch(job -> job.getJobDetail().getKey().equals(jobKey));
    }

    public void triggerJobNow(String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.triggerJob(jobKey);
            log.info("Triggered job immediately: {}", jobName);
        } else {
            log.warn("Job not found for immediate trigger: {}", jobName);
            throw new BatchException(BatchConstants.ErrorCodes.VALIDATION_ERROR, 
                "Job not found: " + jobName);
        }
    }

    public void triggerJobWithParams(String jobName, String jobParam) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName);
        if (scheduler.checkExists(jobKey)) {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("jobName", jobName);
            jobDataMap.put("jobParam", jobParam);
            jobDataMap.put("TaskId", java.util.UUID.randomUUID().toString());
            
            scheduler.triggerJob(jobKey, jobDataMap);
            log.info("Triggered job immediately with params: {} - {}", jobName, jobParam);
        } else {
            log.warn("Job not found for immediate trigger: {}", jobName);
            throw new BatchException(BatchConstants.ErrorCodes.VALIDATION_ERROR, 
                "Job not found: " + jobName);
        }
    }

    public List<SchedulerVo> getScheduledJobs() throws SchedulerException {
        List<SchedulerVo> scheduledJobs = new ArrayList<>();
        
        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(org.quartz.impl.matchers.GroupMatcher.jobGroupEquals(groupName))) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                
                if (!triggers.isEmpty()) {
                    Trigger trigger = triggers.get(0);
                    JobDataMap jobDataMap = jobDetail.getJobDataMap();
                    
                    SchedulerVo schedulerVo = SchedulerVo.builder()
                            .jobName(jobKey.getName())
                            .jobParam(jobDataMap.getString("jobParam"))
                            .jobType(trigger instanceof CronTrigger ? "cron" : "simple")
                            .triggerName(trigger.getKey().getName())
                            .cronExpression(trigger instanceof CronTrigger ? 
                                    ((CronTrigger) trigger).getCronExpression() : null)
                            .repeatInterval(trigger instanceof SimpleTrigger ? 
                                    ((SimpleTrigger) trigger).getRepeatInterval() : null)
                            .build();
                    
                    scheduledJobs.add(schedulerVo);
                }
            }
        }
        
        return scheduledJobs;
    }

    public SchedulerVo getScheduledJob(String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName);
        
        if (!scheduler.checkExists(jobKey)) {
            return null;
        }
        
        JobDetail jobDetail = scheduler.getJobDetail(jobKey);
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        
        if (triggers.isEmpty()) {
            return null;
        }
        
        Trigger trigger = triggers.get(0);
        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        
        return SchedulerVo.builder()
                .jobName(jobKey.getName())
                .jobParam(jobDataMap.getString("jobParam"))
                .jobType(trigger instanceof CronTrigger ? "cron" : "simple")
                .triggerName(trigger.getKey().getName())
                .cronExpression(trigger instanceof CronTrigger ? 
                        ((CronTrigger) trigger).getCronExpression() : null)
                .repeatInterval(trigger instanceof SimpleTrigger ? 
                        ((SimpleTrigger) trigger).getRepeatInterval() : null)
                .build();
    }

    public String getJobStatus(String jobName) throws SchedulerException {
        JobKey jobKey = JobKey.jobKey(jobName);
        
        if (!scheduler.checkExists(jobKey)) {
            return "NOT_SCHEDULED";
        }
        
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
        if (triggers.isEmpty()) {
            return "NO_TRIGGER";
        }
        
        Trigger.TriggerState state = scheduler.getTriggerState(triggers.get(0).getKey());
        
        switch (state) {
            case NORMAL: return "SCHEDULED";
            case PAUSED: return "PAUSED";
            case BLOCKED: return "BLOCKED";
            case ERROR: return "ERROR";
            case COMPLETE: return "COMPLETE";
            default: return "UNKNOWN";
        }
    }
}
