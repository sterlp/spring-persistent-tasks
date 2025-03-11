package org.sterl.spring.persistent_tasks.task.exception;

/**
 * Set the task to cancel and finish the execution
 */
public class CancelTaskException extends TaskException {
    private static final long serialVersionUID = 1L;
    public CancelTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public CancelTaskException(String message) {
        super(message);
    }
}
