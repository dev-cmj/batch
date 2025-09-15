package com.project.batch.scheduler;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Slf4j
@Component
public class SchedulerJobCreator {

    /**
     * JobName으로 스케쥴러 생성
     * @param jobClass
     * @param isDurable
     * @param context
     * @param jobName
     * @return
     */
    public JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable, ApplicationContext context, String jobName) {
        return createJob(jobClass, isDurable, context, jobName, null);
    }

    /**
     * JobDataMap으로 스케쥴러 생성
     *  - JobType이 필요한 경우 해당 메서드 사용
     * @param jobClass
     * @param isDurable
     * @param context
     * @param jobDataMap
     * @return
     */
    public JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable, ApplicationContext context, JobDataMap jobDataMap) {
        return createJob(jobClass, isDurable, context, null, jobDataMap);
    }

    /**
     *
     * @param jobClass
     * @param isDurable
     * @param context
     * @param jobName
     * @param jobDataMap
     * @return
     */
    public JobDetail createJob(Class<? extends QuartzJobBean> jobClass, boolean isDurable, ApplicationContext context, String jobName, JobDataMap jobDataMap) {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        factoryBean.setDurability(isDurable);
        factoryBean.setApplicationContext(context);


        JobDataMap jobDataMapParam = new JobDataMap();

        if (StringUtils.isNotEmpty(jobName)) {
            factoryBean.setName(jobName);
            jobDataMapParam.put("jobName", jobName);
        }else{
            factoryBean.setName((String)jobDataMap.get("jobName"));
            jobDataMapParam.putAll(jobDataMap);
        }

        factoryBean.setJobDataMap(jobDataMapParam);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }

    /**
     * Cron 타입의 스케쥴러 생성
     * @param triggerName
     * @param cronExpression 스케쥴 시간 (정규식으로 생성, ex> "0 0 * * * ?")
     * @param misFireInstruction
     * @return
     */
    public CronTrigger createCronTrigger(String triggerName, String cronExpression, int misFireInstruction) {
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setMisfireInstruction(misFireInstruction);

        try {
            factoryBean.afterPropertiesSet();
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return factoryBean.getObject();
    }

    /**
     * Simple 타입의 스케쥴러 생성
     * @param triggerName
     * @param repeatInterval 스케쥴 시간 ( 초단위 반복 설정 )
     * @param misFireInstruction
     * @return
     */
    public SimpleTrigger createSimpleTrigger(String triggerName, int repeatInterval, int misFireInstruction) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setName(triggerName);
        factoryBean.setRepeatInterval(repeatInterval);
        factoryBean.setMisfireInstruction(misFireInstruction);
        factoryBean.afterPropertiesSet();
        return factoryBean.getObject();
    }
}