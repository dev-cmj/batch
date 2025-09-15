package com.project.batch.constants;

public final class BatchConstants {
    
    private BatchConstants() {}
    
    public static final String JOB_PARAM_JOB_ID = "JobID";
    public static final String JOB_PARAM_JOB_PARAM = "JobParam";
    public static final String JOB_PARAM_TASK_ID = "TaskId";
    
    public static final int DEFAULT_CHUNK_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 1000;
    
    public static final String BATCH_TABLE_PREFIX = "BATCH_";
    
    public static final class ErrorCodes {
        public static final String JOB_EXECUTION_ERROR = "JOB_001";
        public static final String STEP_EXECUTION_ERROR = "STEP_001";
        public static final String DATA_ACCESS_ERROR = "DATA_001";
        public static final String VALIDATION_ERROR = "VALID_001";
        public static final String JOB_NOT_FOUND = "JOB_002";
        
        private ErrorCodes() {}
    }
}