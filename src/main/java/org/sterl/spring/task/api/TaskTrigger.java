package org.sterl.spring.task.api;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * For any registered task a task trigger represent one unit of work, executing this task once.
 */
public record TaskTrigger<T extends Serializable>(
        String id,
        TaskId<T> taskId, T state, 
        OffsetDateTime when, 
        int priority) {

    public static final int DEFAULT_PRIORITY = 4;
}
