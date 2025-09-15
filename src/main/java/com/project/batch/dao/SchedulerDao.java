package com.project.batch.dao;

import com.project.batch.vo.SchedulerVo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SchedulerDao {
    
    @Select("SELECT id, sche_name, job_name, job_param, job_type, cron_expression, trigger_name, repeat_interval, " +
            "use_yn, create_date, create_user, update_date, update_user " +
            "FROM tb_scheduler WHERE sche_name = #{scheName} AND use_yn = #{commonVo.use_yn}")
    List<SchedulerVo> getSchedulerList(SchedulerVo schedulerVo);
    
    @Select("SELECT id, sche_name, job_name, job_param, job_type, cron_expression, trigger_name, repeat_interval, " +
            "use_yn, create_date, create_user, update_date, update_user " +
            "FROM tb_scheduler WHERE job_name = #{jobName}")
    SchedulerVo getSchedulerByJobName(@Param("jobName") String jobName);
    
    @Insert("INSERT INTO tb_scheduler (sche_name, job_name, job_param, job_type, cron_expression, trigger_name, repeat_interval, use_yn, create_user) " +
            "VALUES (#{scheName}, #{jobName}, #{jobParam}, #{jobType}, #{cronExpression}, #{triggerName}, #{repeatInterval}, " +
            "COALESCE(#{useYn}, COALESCE(#{commonVo.use_yn}, 'Y')), COALESCE(#{createUser}, COALESCE(#{commonVo.createUser}, 'SYSTEM')))")
    int insertScheduler(SchedulerVo schedulerVo);
    
    @Update("UPDATE tb_scheduler SET job_param = #{jobParam}, job_type = #{jobType}, cron_expression = #{cronExpression}, " +
            "trigger_name = #{triggerName}, repeat_interval = #{repeatInterval}, " +
            "use_yn = COALESCE(#{useYn}, COALESCE(#{commonVo.use_yn}, use_yn)), " +
            "update_date = CURRENT_TIMESTAMP, update_user = COALESCE(#{updateUser}, COALESCE(#{commonVo.updateUser}, 'SYSTEM')) " +
            "WHERE job_name = #{jobName}")
    int updateScheduler(SchedulerVo schedulerVo);
    
    @Update("UPDATE tb_scheduler SET use_yn = 'N', update_date = CURRENT_TIMESTAMP, update_user = 'SYSTEM' WHERE job_name = #{jobName}")
    int deleteScheduler(@Param("jobName") String jobName);
    
    @Select("SELECT COUNT(*) FROM tb_scheduler WHERE job_name = #{jobName} AND use_yn = 'Y'")
    int countActiveSchedulerByJobName(@Param("jobName") String jobName);
    
    @Select("SELECT id, sche_name, job_name, job_param, job_type, cron_expression, trigger_name, repeat_interval, " +
            "use_yn, create_date, create_user, update_date, update_user " +
            "FROM tb_scheduler WHERE use_yn = 'Y' ORDER BY create_date DESC")
    List<SchedulerVo> getAllActiveSchedulers();
    
    @Select("SELECT id, sche_name, job_name, job_param, job_type, cron_expression, trigger_name, repeat_interval, " +
            "use_yn, create_date, create_user, update_date, update_user " +
            "FROM tb_scheduler ORDER BY create_date DESC")
    List<SchedulerVo> getAllSchedulers();
    
    @Select("SELECT id, sche_name, job_name, job_param, job_type, cron_expression, trigger_name, repeat_interval, " +
            "use_yn, create_date, create_user, update_date, update_user " +
            "FROM tb_scheduler WHERE sche_name = #{scheName} AND use_yn = 'Y' AND job_type = #{jobType}")
    List<SchedulerVo> getSchedulersByType(@Param("scheName") String scheName, @Param("jobType") String jobType);
}