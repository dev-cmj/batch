INSERT INTO person (id, last_name, first_name) VALUES (1, 'Doe', 'John');
INSERT INTO person (id, last_name, first_name) VALUES (2, 'Smith', 'Jane');
INSERT INTO person (id, last_name, first_name) VALUES (3, 'Johnson', 'Peter');
INSERT INTO person (id, last_name, first_name) VALUES (4, 'Williams', 'Mary');
INSERT INTO person (id, last_name, first_name) VALUES (5, 'Brown', 'David');
INSERT INTO person (id, last_name, first_name) VALUES (6, 'Jones', 'Linda');
INSERT INTO person (id, last_name, first_name) VALUES (7, 'Garcia', 'James');
INSERT INTO person (id, last_name, first_name) VALUES (8, 'Miller', 'Patricia');
INSERT INTO person (id, last_name, first_name) VALUES (9, 'Davis', 'Robert');
INSERT INTO person (id, last_name, first_name) VALUES (10, 'Rodriguez', 'Jennifer');
INSERT INTO person (id, last_name, first_name) VALUES (11, 'Martinez', 'Michael');
INSERT INTO person (id, last_name, first_name) VALUES (12, 'Hernandez', 'Elizabeth');

-- Sample scheduler data
INSERT INTO TB_SCHEDULER (sche_name, job_name, job_param, job_type, cron_expression, trigger_name, use_yn, create_user) 
VALUES ('DefaultScheduler', 'jdbcCursorJob', '', 'cron', '0 0 1 * * ?', 'jdbcCursorJobTrigger', 'Y', 'SYSTEM');

INSERT INTO TB_SCHEDULER (sche_name, job_name, job_param, job_type, cron_expression, trigger_name, use_yn, create_user) 
VALUES ('DefaultScheduler', 'jdbcPagingJob', '', 'cron', '0 0 2 * * ?', 'jdbcPagingJobTrigger', 'Y', 'SYSTEM');

INSERT INTO TB_SCHEDULER (sche_name, job_name, job_param, job_type, trigger_name, repeat_interval, use_yn, create_user) 
VALUES ('DefaultScheduler', 'mybatisPagingJob', '', 'simple', 'myBatisPagingJobTrigger', 60000, 'Y', 'SYSTEM');
