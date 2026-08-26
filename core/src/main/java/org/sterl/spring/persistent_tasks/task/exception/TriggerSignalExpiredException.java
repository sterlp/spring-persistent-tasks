package org.sterl.spring.persistent_tasks.task.exception;

/**
 * A trigger scheduled with {@code waitForSignal(...)} was waiting for a signal which
 * did not arrive before the configured timeout.
 * <p>
 * Extends {@link FailTaskNoRetryException} so an expired signal fails the trigger
 * terminally (no retry). Handlers can distinguish it from a regular failure via
 * {@code e instanceof TriggerSignalExpiredException}.
 * </p>
 */
public class TriggerSignalExpiredException extends FailTaskNoRetryException {
    private static final long serialVersionUID = 1L;

    public TriggerSignalExpiredException(String message) {
        super(message);
    }
}
