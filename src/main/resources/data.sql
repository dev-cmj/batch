DELETE FROM TB_SCHEDULER;
-- Sample scheduler data

-- 순차처리 Job (5분마다 실행)
INSERT INTO TB_SCHEDULER (sche_name, job_name, job_param, job_type, cron_expression, trigger_name, use_yn, create_user)
VALUES ('DefaultScheduler', 'sequentialJob', '', 'cron', '0 */5 * * * ?', 'sequentialJobTrigger', 'Y', 'SYSTEM');

-- 병렬처리 Job (7분마다 실행 - 순차처리와 겹치지 않게)
INSERT INTO TB_SCHEDULER (sche_name, job_name, job_param, job_type, cron_expression, trigger_name, use_yn, create_user)
VALUES ('DefaultScheduler', 'parallelJob', '', 'cron', '0 2/7 * * * ?', 'parallelJobTrigger', 'Y', 'SYSTEM');
