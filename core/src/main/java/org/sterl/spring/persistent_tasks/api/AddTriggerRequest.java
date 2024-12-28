package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
/**
 * For any registered task a task trigger represent one unit of work, executing this task once.
 * @param <T> state type which has to be of {@link Serializable}
 */
public record AddTriggerRequest<T extends Serializable>(
        TriggerKey key,
        T state,
        OffsetDateTime runtAt,
        int priority) {
    
    public TaskId<T> taskId() {
        return (TaskId<T>)key.toTaskId();
    }

    public static final int DEFAULT_PRIORITY = 4;
}
