package com.project.batch.repository.impl;

import com.project.batch.constants.BatchConstants;
import com.project.batch.dao.SchedulerDao;
import com.project.batch.exception.BatchException;
import com.project.batch.repository.SchedulerRepository;
import com.project.batch.vo.CommonVo;
import com.project.batch.vo.SchedulerVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SchedulerRepositoryImpl implements SchedulerRepository {
    
    private final SchedulerDao schedulerDao;
    
    @Override
    public List<SchedulerVo> findActiveSchedulers(String scheName) {
        try {
            SchedulerVo searchVo = SchedulerVo.builder()
                    .scheName(scheName)
                    .commonVo(CommonVo.builder().use_yn("Y").build())
                    .build();
            return schedulerDao.getSchedulerList(searchVo);
        } catch (Exception e) {
            log.error("Failed to find active schedulers for scheName: {}", scheName, e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to retrieve active schedulers", e);
        }
    }
    
    @Override
    public Optional<SchedulerVo> findByJobNameOptional(String jobName) {
        try {
            SchedulerVo scheduler = schedulerDao.getSchedulerByJobName(jobName);
            return Optional.ofNullable(scheduler);
        } catch (Exception e) {
            log.error("Failed to find scheduler by jobName: {}", jobName, e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to find scheduler by job name", e);
        }
    }
    
    @Override
    public SchedulerVo findByJobName(String jobName) {
        try {
            return schedulerDao.getSchedulerByJobName(jobName);
        } catch (Exception e) {
            log.error("Failed to find scheduler by jobName: {}", jobName, e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to find scheduler by job name", e);
        }
    }
    
    @Override
    public List<SchedulerVo> findAll() {
        try {
            return schedulerDao.getAllSchedulers();
        } catch (Exception e) {
            log.error("Failed to find all schedulers", e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to retrieve all schedulers", e);
        }
    }
    
    @Override
    public void update(SchedulerVo schedulerVo) {
        try {
            int result = schedulerDao.updateScheduler(schedulerVo);
            if (result == 0) {
                throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                    "Failed to update scheduler: " + schedulerVo.getJobName());
            }
            log.info("Updated scheduler: {}", schedulerVo.getJobName());
        } catch (Exception e) {
            log.error("Failed to update scheduler: {}", schedulerVo.getJobName(), e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to update scheduler", e);
        }
    }
    
    @Override
    public SchedulerVo save(SchedulerVo schedulerVo) {
        try {
            if (existsActiveScheduler(schedulerVo.getJobName())) {
                int result = schedulerDao.updateScheduler(schedulerVo);
                if (result == 0) {
                    throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                        "Failed to update scheduler: " + schedulerVo.getJobName());
                }
                log.info("Updated scheduler: {}", schedulerVo.getJobName());
            } else {
                int result = schedulerDao.insertScheduler(schedulerVo);
                if (result == 0) {
                    throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                        "Failed to insert scheduler: " + schedulerVo.getJobName());
                }
                log.info("Inserted new scheduler: {}", schedulerVo.getJobName());
            }
            return schedulerVo;
        } catch (Exception e) {
            log.error("Failed to save scheduler: {}", schedulerVo.getJobName(), e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to save scheduler", e);
        }
    }
    
    @Override
    public void delete(String jobName) {
        try {
            int result = schedulerDao.deleteScheduler(jobName);
            if (result == 0) {
                log.warn("No scheduler found to delete with jobName: {}", jobName);
            } else {
                log.info("Deleted scheduler: {}", jobName);
            }
        } catch (Exception e) {
            log.error("Failed to delete scheduler: {}", jobName, e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to delete scheduler", e);
        }
    }
    
    @Override
    public boolean existsActiveScheduler(String jobName) {
        try {
            return schedulerDao.countActiveSchedulerByJobName(jobName) > 0;
        } catch (Exception e) {
            log.error("Failed to check if scheduler exists: {}", jobName, e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to check scheduler existence", e);
        }
    }
    
    @Override
    public List<SchedulerVo> findAllActive() {
        try {
            return schedulerDao.getAllActiveSchedulers();
        } catch (Exception e) {
            log.error("Failed to find all active schedulers", e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to retrieve all active schedulers", e);
        }
    }
    
    @Override
    public List<SchedulerVo> findByScheduleNameAndType(String scheName, String jobType) {
        try {
            return schedulerDao.getSchedulersByType(scheName, jobType);
        } catch (Exception e) {
            log.error("Failed to find schedulers by type - scheName: {}, jobType: {}", scheName, jobType, e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to find schedulers by type", e);
        }
    }
    
    @Override
    public void updateSchedulerStatus(String jobName, String status) {
        try {
            SchedulerVo schedulerVo = schedulerDao.getSchedulerByJobName(jobName);
            if (schedulerVo != null) {
                schedulerVo.setUseYn(status);
                schedulerVo.setUpdateUser("SYSTEM");
                schedulerDao.updateScheduler(schedulerVo);
                log.info("Updated scheduler status - jobName: {}, status: {}", jobName, status);
            }
        } catch (Exception e) {
            log.error("Failed to update scheduler status - jobName: {}, status: {}", jobName, status, e);
            throw new BatchException(BatchConstants.ErrorCodes.DATA_ACCESS_ERROR, 
                "Failed to update scheduler status", e);
        }
    }
}