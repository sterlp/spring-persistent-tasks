package org.sterl.spring.persistent_tasks.api.task.exception;

public abstract class TaskException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public TaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskException(String message) {
        super(message);
    }
}
