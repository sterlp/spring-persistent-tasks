package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;

/**
/**
 * For any registered persistentTask a persistentTask trigger represent one unit of work, executing this persistentTask once.
 * @param <T> state type which has to be of {@link Serializable}
 */
public record TriggerRequest<T extends Serializable>(
        TriggerKey key,
        TriggerStatus status,
        T state,
        OffsetDateTime runtAt,
        int priority,
        String correlationId,
        String tag) {
    
    @SuppressWarnings("unchecked")
    public TaskId<T> taskId() {
        return (TaskId<T>)key.toTaskId();
    }

    public Collection<TriggerRequest<T>> toList() {
        return Collections.singleton(this);
    }

    public static final int DEFAULT_PRIORITY = 4;
}
