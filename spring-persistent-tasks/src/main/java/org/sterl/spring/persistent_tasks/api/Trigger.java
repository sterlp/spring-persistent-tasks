package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * For any registered task a task trigger represent one unit of work, executing this task once.
 */
public record Trigger<T extends Serializable>(
        String id,
        TaskId<T> taskId,
        T state,
        OffsetDateTime when,
        int priority) {

    public static final int DEFAULT_PRIORITY = 4;

    public TriggerId toTaskTriggerId() {
        return new TriggerId(id, taskId.name());
    }
}
