package com.project.batch.exception;

public class BatchException extends RuntimeException {
    
    private final String errorCode;
    
    public BatchException(String message) {
        super(message);
        this.errorCode = "BATCH_ERROR";
    }
    
    public BatchException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BatchException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BATCH_ERROR";
    }
    
    public BatchException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}