package org.sterl.spring.persistent_tasks.exception;

import lombok.Getter;

public class SpringPersistentTaskException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    @Getter
    protected final Object state;

    public SpringPersistentTaskException(String message, Object state, Throwable cause) {
        super(message, cause);
        this.state = state;
    }

    public SpringPersistentTaskException(String message, Object state) {
        super(message);
        this.state = state;
    }
}
