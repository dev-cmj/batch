package com.project.batch.repository;

import com.project.batch.vo.SchedulerVo;

import java.util.List;
import java.util.Optional;

public interface SchedulerRepository {
    
    List<SchedulerVo> findActiveSchedulers(String scheName);
    
    Optional<SchedulerVo> findByJobNameOptional(String jobName);
    
    SchedulerVo save(SchedulerVo schedulerVo);
    
    void delete(String jobName);
    
    boolean existsActiveScheduler(String jobName);
    
    List<SchedulerVo> findAllActive();
    
    List<SchedulerVo> findByScheduleNameAndType(String scheName, String jobType);
    
    void updateSchedulerStatus(String jobName, String status);
    
    List<SchedulerVo> findAll();
    
    SchedulerVo findByJobName(String jobName);
    
    void update(SchedulerVo schedulerVo);
}