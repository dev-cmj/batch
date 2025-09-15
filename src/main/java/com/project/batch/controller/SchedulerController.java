package com.project.batch.controller;

import com.project.batch.exception.BatchException;
import com.project.batch.repository.SchedulerRepository;
import com.project.batch.scheduler.SchedulerService;
import com.project.batch.vo.SchedulerVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;
    private final SchedulerRepository schedulerRepository;

    @GetMapping
    public ResponseEntity<List<SchedulerVo>> getAllSchedulers() {
        try {
            List<SchedulerVo> schedulers = schedulerRepository.findAll();
            return ResponseEntity.ok(schedulers);
        } catch (Exception e) {
            log.error("Failed to get all schedulers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{jobName}")
    public ResponseEntity<SchedulerVo> getScheduler(@PathVariable String jobName) {
        try {
            SchedulerVo scheduler = schedulerRepository.findByJobName(jobName);
            if (scheduler == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(scheduler);
        } catch (Exception e) {
            log.error("Failed to get scheduler: {}", jobName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<String> createScheduler(@RequestBody SchedulerVo schedulerVo) {
        try {
            schedulerRepository.save(schedulerVo);
            return ResponseEntity.status(HttpStatus.CREATED).body("Scheduler created successfully");
        } catch (Exception e) {
            log.error("Failed to create scheduler: {}", schedulerVo.getJobName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create scheduler: " + e.getMessage());
        }
    }

    @PutMapping("/{jobName}")
    public ResponseEntity<String> updateScheduler(@PathVariable String jobName, 
                                                 @RequestBody SchedulerVo schedulerVo) {
        try {
            SchedulerVo existingScheduler = schedulerRepository.findByJobName(jobName);
            if (existingScheduler == null) {
                return ResponseEntity.notFound().build();
            }
            
            schedulerVo.setJobName(jobName);
            schedulerRepository.update(schedulerVo);
            return ResponseEntity.ok("Scheduler updated successfully");
        } catch (Exception e) {
            log.error("Failed to update scheduler: {}", jobName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update scheduler: " + e.getMessage());
        }
    }

    @DeleteMapping("/{jobName}")
    public ResponseEntity<String> deleteScheduler(@PathVariable String jobName) {
        try {
            SchedulerVo existingScheduler = schedulerRepository.findByJobName(jobName);
            if (existingScheduler == null) {
                return ResponseEntity.notFound().build();
            }
            
            schedulerRepository.delete(jobName);
            return ResponseEntity.ok("Scheduler deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete scheduler: {}", jobName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete scheduler: " + e.getMessage());
        }
    }

    @PostMapping("/{jobName}/start")
    public ResponseEntity<String> registerScheduler(@PathVariable String jobName) {
        try {
            SchedulerVo scheduler = schedulerRepository.findByJobName(jobName);
            if (scheduler == null) {
                return ResponseEntity.notFound().build();
            }

            schedulerService.scheduleJob(scheduler);
            return ResponseEntity.ok("Scheduler registered successfully");
        } catch (Exception e) {
            log.error("Failed to register scheduler: {}", jobName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to register scheduler: " + e.getMessage());
        }
    }

    @PostMapping("/{jobName}/trigger")
    public ResponseEntity<String> startScheduledJob(@PathVariable String jobName) {
        try {
            // 1. DB에서 해당 job 정보 확인
            SchedulerVo scheduler = schedulerRepository.findByJobName(jobName);
            if (scheduler == null) {
                return ResponseEntity.notFound().build();
            }

            // 2. 스케줄링된 job인지 확인
            SchedulerVo scheduledJob = schedulerService.getScheduledJob(jobName);
            if (scheduledJob == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Job is not scheduled: " + jobName);
            }

            // 3. 두 조건 모두 통과하면 job 실행
            schedulerService.triggerJobNow(jobName);
            return ResponseEntity.ok("Scheduled job started successfully");
        } catch (Exception e) {
            log.error("Failed to start scheduled job: {}", jobName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start scheduled job: " + e.getMessage());
        }
    }

    @PostMapping("/{jobName}/trigger-with-params")
    public ResponseEntity<String> triggerJobWithParams(@PathVariable String jobName, 
                                                      @RequestParam String jobParam) {
        try {
            schedulerService.triggerJobWithParams(jobName, jobParam);
            return ResponseEntity.ok("Job triggered with parameters successfully");
        } catch (Exception e) {
            log.error("Failed to trigger job with params: {} - {}", jobName, jobParam, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to trigger job with parameters: " + e.getMessage());
        }
    }

    @PostMapping("/{jobName}/pause")
    public ResponseEntity<String> pauseScheduler(@PathVariable String jobName) {
        try {
            schedulerService.pauseJob(jobName);
            return ResponseEntity.ok("Scheduler paused successfully");
        } catch (SchedulerException e) {
            log.error("Failed to pause scheduler: {}", jobName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to pause scheduler: " + e.getMessage());
        }
    }

    @PostMapping("/{jobName}/resume")
    public ResponseEntity<String> resumeScheduler(@PathVariable String jobName) {
        try {
            schedulerService.resumeJob(jobName);
            return ResponseEntity.ok("Scheduler resumed successfully");
        } catch (SchedulerException e) {
            log.error("Failed to resume scheduler: {}", jobName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to resume scheduler: " + e.getMessage());
        }
    }

    @GetMapping("/{jobName}/status")
    public ResponseEntity<String> getSchedulerStatus(@PathVariable String jobName) {
        try {
            String status = schedulerService.getJobStatus(jobName);
            return ResponseEntity.ok(status);
        } catch (SchedulerException e) {
            log.error("Failed to get scheduler status: {}", jobName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get scheduler status: " + e.getMessage());
        }
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<SchedulerVo>> getScheduledJobs() {
        try {
            List<SchedulerVo> scheduledJobs = schedulerService.getScheduledJobs();
            return ResponseEntity.ok(scheduledJobs);
        } catch (Exception e) {
            log.error("Failed to get scheduled jobs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{jobName}/scheduled")
    public ResponseEntity<SchedulerVo> getScheduledJob(@PathVariable String jobName) {
        try {
            SchedulerVo scheduledJob = schedulerService.getScheduledJob(jobName);
            if (scheduledJob == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(scheduledJob);
        } catch (Exception e) {
            log.error("Failed to get scheduled job: {}", jobName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/stop-all")
    public ResponseEntity<String> stopAllSchedulers() {
        try {
            schedulerService.stopAllSchedulers();
            return ResponseEntity.ok("All schedulers stopped successfully");
        } catch (Exception e) {
            log.error("Failed to stop all schedulers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to stop all schedulers: " + e.getMessage());
        }
    }
}