package org.sterl.spring.persistent_tasks.api;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * For any registered task a task trigger represent one unit of work, executing this task once.
 */
public record AddTriggerRequest<T extends Serializable>(
        String id,
        TaskId<T> taskId,
        T state,
        OffsetDateTime runtAt,
        int priority) {

    public static final int DEFAULT_PRIORITY = 4;

    public TriggerKey toTaskTriggerId() {
        return new TriggerKey(id, taskId.name());
    }
}
