package org.sterl.spring.persistent_tasks.task.exception;

/**
 * Set the task to failed and finish the execution.
 */
public class FailTaskNoRetryException extends TaskException {
    private static final long serialVersionUID = 1L;

    public FailTaskNoRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailTaskNoRetryException(String message) {
        super(message);
    }
}
