package com.project.batch.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerVo {
    
    private Long id;
    private String scheName;
    private String jobName;
    private String jobParam;
    private String jobType;
    private String cronExpression;
    private String triggerName;
    private Long repeatInterval;
    private String useYn;
    private String createDate;
    private String createUser;
    private String updateDate;
    private String updateUser;
    private CommonVo commonVo;
    
    public boolean isCronType() {
        return "cron".equalsIgnoreCase(this.jobType);
    }
    
    public boolean isSimpleType() {
        return "simple".equalsIgnoreCase(this.jobType);
    }
}